package icbm.classic.content.entity;

import icbm.classic.ICBMClassic;
import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.actions.IAction;
import icbm.classic.api.explosion.IBlastInit;
import icbm.classic.api.explosion.IBlastRestore;
import icbm.classic.api.explosion.IBlastTickable;
import icbm.classic.api.reg.IExplosiveData;
import icbm.classic.content.blast.Blast;
import icbm.classic.content.missile.logic.source.ActionSource;
import icbm.classic.content.missile.logic.source.cause.EntityCause;
import icbm.classic.lib.NBTConstants;
import lombok.Getter;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MoverType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.network.NetworkHooks;

/**
 * The Entity handler responsible for entity explosions.
 *
 * @author Calclavia
 */
@Deprecated //TODO replace all usage with more focused entities per explosive
public class EntityExplosion extends Entity implements IEntityAdditionalSpawnData
{
    @Getter
    private IAction blast;
    private double blastYOffset = 0;

    public EntityExplosion(EntityType<EntityExplosion> type, World world)
    {
        super(type, world);
        this.preventEntitySpawning = true;
        this.noClip = true;
        this.ignoreFrustumCheck = true;
        this.ticksExisted = 0;
    }

    @Override
    public IPacket<?> createSpawnPacket()
    {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public void writeSpawnData(PacketBuffer data)
    {
        data.writeResourceLocation(blast.getActionData().getRegistryKey());
        data.writeDouble(blastYOffset);
    }

    @Override
    public void readSpawnData(PacketBuffer data)
    {
        constructBlast(data.readResourceLocation(), data.readDouble());
    }

    @Override
    protected void registerData()
    {
    }

    /**
     * returns if this entity triggers Block.onEntityWalking on the blocks they walk on. used for
     * spiders and wolves to prevent them from trampling crops
     */
    @Override
    protected boolean canTriggerWalking()
    {
        return false;
    }

    /** Returns true if other Entities should be prevented from moving through this Entity. */
    @Override
    public boolean canBeCollidedWith()
    {
        return false;
    }

    /** Called to update the entity's position/logic. */
    @Override
    public void tick()
    {
        if (!(this.getBlast() instanceof IBlastTickable) || ((IBlastTickable)this.getBlast()).getEntity() != this || ((IBlastTickable)this.getBlast()).isCompleted())
        {
            this.tick();
            return;
        }

        if (blast instanceof IBlastTickable && ((IBlastTickable) blast).onBlastTick(ticksExisted))
        {
            remove();
        }
    }

    @Override
    public void move(MoverType typeIn, Vec3d pos)
    {
        //Remove default movement
    }

    /** (abstract) Protected helper method to read subclass entity data from NBT. */
    @Override
    protected void readAdditional(CompoundNBT nbt)
    {
        try
        {
            CompoundNBT blastSave = nbt.getCompound(NBTConstants.BLAST);
            this.blastYOffset = nbt.getDouble(NBTConstants.BLAST_POS_Y);
            if (getBlast() == null)
            {
                if (blastSave.contains(NBTConstants.EX_ID))
                {
                    constructBlast(new ResourceLocation(blastSave.getString(NBTConstants.EX_ID)), blastYOffset);
                }
                else
                {
                    ICBMClassic.logger().error("EntityExplosion: Failed to read save state for explosion!");
                    remove();
                }
            }

            if (getBlast() instanceof IBlastRestore)
            {
                ((IBlastRestore) getBlast()).load(blastSave);
            }
        }
        catch (Exception e)
        {
            ICBMClassic.logger().error("EntityExplosion: Unexpected error restoring save state of explosion entity!", e);
        }
    }

    /** (abstract) Protected helper method to write subclass entity data to NBT. */
    @Override
    protected void writeAdditional(CompoundNBT nbt)
    {
        if (getBlast() != null) //TODO add save/load mechanic to bypass need for ex data
        {
            //Save position
            nbt.putDouble(NBTConstants.BLAST_POS_Y, blastYOffset);

            //Save explosive data
            CompoundNBT blastSave = new CompoundNBT();
            if (getBlast() instanceof IBlastRestore)
            {
                ((IBlastRestore) getBlast()).save(blastSave);
            }
            blastSave.putString(NBTConstants.EX_ID, getBlast().getActionData().getRegistryKey().toString());

            //Encode into NBT
            nbt.put(NBTConstants.BLAST, blastSave);
        }
    }

    public void setBlast(Blast blast)
    {
        this.blast = blast;
        if (blast != null)
        {
            ((Blast) this.blast).setEntityController(this);
            this.setPosition(blast.getPosition().x, !blast.isMovable() ? -1 : blast.getPosition().y, blast.getPosition().z);
            blastYOffset = blast.isMovable() ? 0 : blast.getPosition().y + 1;
        }
    }

    /**
     * Constructs a blast based on the parameters and sets the blast field to that value
     */
    private void constructBlast(ResourceLocation id, double yOffset)
    {
        IExplosiveData exData = ICBMClassicAPI.EXPLOSIVE_REGISTRY.getExplosiveData(id, true);

        if(exData == null) {
            ICBMClassic.logger().error("EntityExplosion: Failed to locate explosive with id '{}'!", id);
            this.remove();
            return;
        }

        ActionSource actionSource = new ActionSource(DimensionType.getKey(world.dimension.getType()), new Vec3d(posX, posY + yOffset, posZ), new EntityCause(this)); //TODO provide additional cause information such as fire, lighter, player, etc
        blast = exData.create(world, posX, posY + yOffset, posZ, actionSource, null);

        if(blast instanceof IBlastInit) {
            ((IBlastInit) blast).setEntityController(this);
            blast = ((IBlastInit) blast).buildBlast();
        }
    }
}
