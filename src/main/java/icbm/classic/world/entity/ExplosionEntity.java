package icbm.classic.world.entity;

import icbm.classic.ICBMClassic;
import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.explosion.*;
import icbm.classic.api.reg.ExplosiveType;
import icbm.classic.config.ConfigDebug;
import icbm.classic.lib.NBTConstants;
import icbm.classic.world.blast.Blast;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.Level;
import net.neoforged.fml.common.network.ByteBufUtils;

import java.lang.reflect.Constructor;

/**
 * The Entity handler responsible for entity explosions.
 *
 * @author Calclavia
 */
@Deprecated //TODO replace all usage with more focused entities per explosive
public class ExplosionEntity extends Entity {
    private IBlast blast;
    private double blastYOffset = 0;

    public ExplosionEntity(EntityType<?> type, Level level) {
        super(type, level);
        this.preventEntitySpawning = true;
        this.noClip = true;
        this.setSize(0.98F, 0.98F);
        this.ignoreFrustumCheck = true;
        this.ticksExisted = 0;
    }

    public ExplosionEntity(Blast blast) {
        this(blast.level());
        this.setBlast(blast);
        if (ConfigDebug.DEBUG_EXPLOSIVES) {
            ICBMClassic.logger().info("EntityExplosion#new(" + blast + ") Created new blast controller entity");
        }
    }

    @Override
    public String getName() {
        return "Explosion[" + blast + "]";
    }

    @Override
    public void writeSpawnData(ByteBuf data) {
        ByteBufUtils.writeUTF8String(data, blast.getExplosiveData().getRegistryName().toString());
        data.writeDouble(blastYOffset);
    }

    @Override
    public void readSpawnData(ByteBuf data) {
        constructBlast(ByteBufUtils.readUTF8String(data), data.readDouble());
    }

    @Override
    protected void entityInit() {
    }

    /**
     * returns if this entity triggers Block.onEntityWalking on the blocks they walk on. used for
     * spiders and wolves to prevent them from trampling crops
     */
    @Override
    protected boolean canTriggerWalking() {
        return false;
    }

    /**
     * Returns true if other Entities should be prevented from moving through this Entity.
     */
    @Override
    public boolean canBeCollidedWith() {
        return false;
    }

    /**
     * Called to update the entity's position/logic.
     */
    @Override
    public void onUpdate() {
        if (this.getBlast() == null || this.getBlast().getEntity() != this || this.getBlast().isCompleted()) {
            this.setDead();
            return;
        }

        if (this.getBlast() instanceof IBlastMovable && (this.motionX != 0 || this.motionY != 0 || this.motionZ != 0)) {
            //Slow entity down
            this.motionX *= .98;
            this.motionY *= .98;
            this.motionZ *= .98;

            //Normalize
            float speed = MathHelper.sqrt(motionX * motionX + motionY * motionY + motionZ * motionZ);
            this.motionX /= (double) speed;
            this.motionY /= (double) speed;
            this.motionZ /= (double) speed;

            //Apply Speed
            speed = Math.min(speed, 0.5f);
            this.motionX *= (double) speed;
            this.motionY *= (double) speed;
            this.motionZ *= (double) speed;

            //Move box
            this.setEntityBoundingBox(this.getEntityBoundingBox().offset(motionX, motionY, motionZ));

            //Reset position based on box
            this.getX() = (this.getEntityBoundingBox().minX + this.getEntityBoundingBox().maxX) / 2.0D;
            this.getY() = (this.getEntityBoundingBox().minY + this.getEntityBoundingBox().maxY) / 2.0D;
            this.getZ() = (this.getEntityBoundingBox().minZ + this.getEntityBoundingBox().maxZ) / 2.0D;

            //Update blast
            ((IBlastMovable) getBlast()).onPositionUpdate(posX, posY + blastYOffset, posZ);
        }

        if (blast instanceof IBlastTickable) {
            if (((IBlastTickable) blast).onBlastTick(ticksExisted)) {
                setDead();
            }
        }
    }

    @Override
    public void move(MoverType type, double p_70091_1_, double p_70091_3_, double p_70091_5_) {
        //Remove default movement
    }

    /**
     * (abstract) Protected helper method to read subclass entity data from NBT.
     */
    @Override
    protected void readEntityFromNBT(CompoundTag nbt) {
        try {
            CompoundTag blastSave = nbt.getCompound(NBTConstants.BLAST);
            this.blastYOffset = nbt.getDouble(NBTConstants.BLAST_POS_Y);
            if (getBlast() == null) {
                //Legacy code
                if (blastSave.contains(NBTConstants.CLASS)) {
                    Class clazz = Class.forName(blastSave.getString(NBTConstants.CLASS));
                    Constructor constructor = clazz.getConstructor();
                    Blast blast = (Blast) constructor.newInstance();
                    blast.setBlastLevel(world);
                    blast.setPosition(posX, posY + blastYOffset, posZ);
                    blast.setEntityController(this);
                    blast.buildBlast();
                } else if (blastSave.contains(NBTConstants.EX_ID)) {
                    constructBlast(blastSave.getString(NBTConstants.EX_ID), blastYOffset);
                } else {
                    ICBMClassic.logger().error("EntityExplosion: Failed to read save state for explosion!");
                }
            }

            if (getBlast() instanceof IBlastRestore) {
                ((IBlastRestore) getBlast()).load(blastSave);
            }
        } catch (Exception e) {
            ICBMClassic.logger().error("EntityExplosion: Unexpected error restoring save state of explosion entity!", e);
        }
    }

    /**
     * (abstract) Protected helper method to write subclass entity data to NBT.
     */
    @Override
    protected void writeEntityToNBT(CompoundTag nbt) {
        if (getBlast() != null && getBlast().getExplosiveData() != null) //TODO add save/load mechanic to bypass need for ex data
        {
            //Save position
            nbt.setDouble(NBTConstants.BLAST_POS_Y, blastYOffset);

            //Save explosive data
            CompoundTag blastSave = new CompoundTag();
            if (getBlast() instanceof IBlastRestore) {
                ((IBlastRestore) getBlast()).save(blastSave);
            }
            blastSave.putString(NBTConstants.EX_ID, getBlast().getExplosiveData().getRegistryName().toString());

            //Encode into NBT
            nbt.put(NBTConstants.BLAST, blastSave);
        }
    }

    public IBlast getBlast() {
        return blast;
    }

    public void setBlast(Blast blast) {
        this.blast = blast;
        if (blast != null) {
            if (blast instanceof IBlastInit) {
                ((Blast) this.blast).setEntityController(this);
            }
            this.setPosition(blast.location.x(), !blast.isMovable() ? -1 : blast.y(), blast.location.z());
            blastYOffset = blast.isMovable() ? 0 : blast.y() + 1;
        }
    }

    /**
     * Constructs a blast based on the parameters and sets the blast field to that value
     */
    private void constructBlast(String exId, double yOffset) {
        ResourceLocation id = new ResourceLocation(exId);
        ExplosiveType exData = ICBMClassicAPI.EXPLOSIVE_REGISTRY.getExplosiveData(id);
        if (exData != null) {
            final IBlastFactory factory = exData.getBlastFactory(); //TODO convert load code to blast creation helper
            if (factory != null) {
                blast = factory.create();
                ((IBlastInit) blast).setBlastLevel(world);
                ((IBlastInit) blast).setBlastPosition(posX, posY + yOffset, posZ);
                ((IBlastInit) blast).setEntityController(this);
                ((IBlastInit) blast).setExplosiveData(exData);
                ((IBlastInit) blast).buildBlast();
                return;
            }
        }

        ICBMClassic.logger().error("EntityExplosion: Failed to locate explosive with id '" + id + "'!");
    }
}
