package icbm.classic.content.entity;

import icbm.classic.ICBMClassic;
import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.actions.IAction;
import icbm.classic.api.explosion.*;
import icbm.classic.api.reg.IExplosiveData;
import icbm.classic.config.ConfigDebug;
import icbm.classic.content.blast.Blast;
import icbm.classic.content.missile.logic.source.ActionSource;
import icbm.classic.content.missile.logic.source.cause.EntityCause;
import icbm.classic.lib.NBTConstants;
import icbm.classic.lib.actions.WorkTickingActionHandler;
import lombok.Getter;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import icbm.classic.content.reg.EntityReg;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.entity.IEntityWithComplexSpawn;

import java.lang.reflect.Constructor;

/**
 * The Entity handler responsible for entity explosions.
 *
 * @author Calclavia
 */
@Deprecated //TODO replace all usage with more focused entities per explosive
public class EntityExplosion extends Entity implements IEntityWithComplexSpawn
{
    @Getter
    private IAction blast;
    private double blastYOffset = 0;

    public EntityExplosion(EntityType<? extends EntityExplosion> type, Level world)
    {
        super(type, world);
        // PORT: 1.12 parked non-movable blasts at y=-1 below the world, since 1.18 that is buildable space so the
        // invisible controller must not block block placement
        this.blocksBuilding = false;
        this.noPhysics = true;
        this.noCulling = true;
        this.tickCount = 0;
    }

    public EntityExplosion(Level world)
    {
        this(EntityReg.EXPLOSION.get(), world);
    }

    public EntityExplosion(Blast blast)
    {
        this(blast.world());
        this.setBlast(blast);
        if (ConfigDebug.DEBUG_EXPLOSIVES)
        {
            ICBMClassic.logger().info("EntityExplosion#new({}) Created new blast controller entity", blast);
        }
    }

    @Override
    public Component getName()
    {
        return Component.literal("Explosion[" + blast + "]");
    }

    @Override
    public void writeSpawnData(RegistryFriendlyByteBuf data)
    {
        data.writeUtf(blast.getActionData().getRegistryKey().toString());
        data.writeDouble(blastYOffset);
    }

    @Override
    public void readSpawnData(RegistryFriendlyByteBuf data)
    {
        constructBlast(data.readUtf(), data.readDouble());
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder)
    {
    }

    /**
     * returns if this entity triggers Block.onEntityWalking on the blocks they walk on. used for
     * spiders and wolves to prevent them from trampling crops
     */
    @Override
    protected Entity.MovementEmission getMovementEmission()
    {
        return Entity.MovementEmission.NONE;
    }

    /** Returns true if other Entities should be prevented from moving through this Entity. */
    @Override
    public boolean isPickable()
    {
        return false;
    }

    /** Called to update the entity's position/logic. */
    @Override
    public void tick()
    {
        if (!(this.getBlast() instanceof IBlastTickable) || ((IBlastTickable)this.getBlast()).getEntity() != this || ((IBlastTickable)this.getBlast()).isCompleted())
        {
            this.discard();
            return;
        }

        if (this.getBlast() instanceof IBlastMovable && !this.getDeltaMovement().equals(Vec3.ZERO))
        {
            //Slow entity down
            Vec3 motion = this.getDeltaMovement().scale(.98);

            //Normalize
            float speed = (float) motion.length();
            motion = motion.scale(1.0 / speed);

            //Apply Speed
            speed = Math.min(speed, 0.5f);
            motion = motion.scale(speed);
            this.setDeltaMovement(motion);

            //Move, 1.12 offset the bounding box then centered the position on it
            this.setPos(this.getX() + motion.x, this.getY() + motion.y, this.getZ() + motion.z);

            //Update blast
            ((IBlastMovable) getBlast()).onPositionUpdate(getX(), getY() + blastYOffset, getZ());
        }

        if (blast instanceof IBlastTickable && ((IBlastTickable) blast).onBlastTick(tickCount))
        {
            discard();
        }
    }

    @Override
    public void move(MoverType type, Vec3 movement)
    {
        //Remove default movement
    }

    @Override
    public void onRemovedFromLevel()
    {
        super.onRemovedFromLevel();
        // PORT: an explosion unloaded with its chunk rebuilds a new Blast from save on load, drop the old one from the
        // static tracker so it does not leak until the server stops. Client guard: in single player both threads share
        // the static, unsynchronised list. Removing a blast that is not tracked does nothing.
        if (!level().isClientSide() && blast instanceof Blast b && b.getEntity() == this)
        {
            WorkTickingActionHandler.remove(b);
        }
    }

    /** (abstract) Protected helper method to read subclass entity data from NBT. */
    @Override
    protected void readAdditionalSaveData(CompoundTag nbt)
    {
        try
        {
            CompoundTag blastSave = nbt.getCompound(NBTConstants.BLAST);
            this.blastYOffset = nbt.getDouble(NBTConstants.BLAST_POS_Y);
            if (getBlast() == null)
            {
                if (blastSave.contains(NBTConstants.EX_ID))
                {
                    constructBlast(blastSave.getString(NBTConstants.EX_ID), blastYOffset);
                }
                else
                {
                    ICBMClassic.logger().error("EntityExplosion: Failed to read save state for explosion!");
                    discard();
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
    protected void addAdditionalSaveData(CompoundTag nbt)
    {
        if (getBlast() != null) //TODO add save/load mechanic to bypass need for ex data
        {
            //Save position
            nbt.putDouble(NBTConstants.BLAST_POS_Y, blastYOffset);

            //Save explosive data
            CompoundTag blastSave = new CompoundTag();
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
            this.setPos(blast.location.x(), !blast.isMovable() ? -1 : blast.y(), blast.location.z());
            blastYOffset = blast.isMovable() ? 0 : blast.y() + 1;
        }
    }

    /**
     * Constructs a blast based on the parameters and sets the blast field to that value
     */
    private void constructBlast(String exId, double yOffset)
    {
        ResourceLocation id = ResourceLocation.parse(exId);
        IExplosiveData exData = ICBMClassicAPI.EXPLOSIVE_REGISTRY.getExplosiveData(id, true);

        if(exData == null) {
            ICBMClassic.logger().error("EntityExplosion: Failed to locate explosive with id '{}'!", id);
            this.discard();
            return;
        }

        ActionSource actionSource = new ActionSource(level(), new Vec3(getX(), getY() + yOffset, getZ()), new EntityCause(this)); //TODO provide additional cause information such as fire, lighter, player, etc
        blast = exData.create(level(), getX(), getY() + yOffset, getZ(), actionSource, null);

        if(blast instanceof IBlastInit) {
            ((IBlastInit) blast).setEntityController(this);
            blast = ((IBlastInit) blast).buildBlast();
        }
    }
}
// PORTED
