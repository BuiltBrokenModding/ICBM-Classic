package icbm.classic.content.blast.redmatter;

import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.config.blast.ConfigBlast;
import icbm.classic.content.blast.redmatter.caps.BlastRedmatterWrapper;
import icbm.classic.content.blast.redmatter.caps.CapRedmatterPull;
import icbm.classic.content.blast.redmatter.logic.RedmatterLogic;
import icbm.classic.content.blast.redmatter.render.RedmatterClientLogic;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.Vec3;
import icbm.classic.content.reg.EntityReg;
import icbm.classic.lib.capability.holder.ICapabilityHolderEntity;
import net.neoforged.neoforge.capabilities.EntityCapability;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

/**
 * Created by Dark(DarkGuardsman, Robin) on 4/19/2020.
 */
public class EntityRedmatter extends Entity implements ICapabilityHolderEntity
{
    public static final String NBT_BLAST_SIZE = "blast_size";
    public static final String NBT_BLAST_SIZE_MAX = "blast_size_max";


    public static final float MAX_SPEED = 0.5f;
    public static final float SPEED_REDUCTION = 0.98f;

    //Acts as an API wrapper for the entity
    public final BlastRedmatterWrapper blastData = new BlastRedmatterWrapper(this);
    public final CapRedmatterPull capRedmatterPull = new CapRedmatterPull(this);

    //Handlers
    public final RedmatterClientLogic clientLogic = new RedmatterClientLogic(this);
    public final RedmatterLogic redmatterLogic = new RedmatterLogic(this);

    /** Actual size of the redmatter */
    private static final EntityDataAccessor<Float> SIZE_DATA = SynchedEntityData.defineId(EntityRedmatter.class, EntityDataSerializers.FLOAT);
    /** Largest possible size of the redmatter */
    private static final EntityDataAccessor<Float> MAX_SIZE_DATA = SynchedEntityData.defineId(EntityRedmatter.class, EntityDataSerializers.FLOAT);

    public EntityRedmatter(EntityType<? extends EntityRedmatter> type, Level world)
    {
        super(type, world);
        this.blocksBuilding = true;
        this.noCulling = true;
        this.tickCount = 0;
        this.noPhysics = true;
    }

    public EntityRedmatter(Level world)
    {
        this(EntityReg.REDMATTER.get(), world);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder)
    {
        builder.define(SIZE_DATA, ConfigBlast.redmatter.DEFAULT_SIZE);
        builder.define(MAX_SIZE_DATA, ConfigBlast.redmatter.MAX_SIZE);
    }

    @Override
    public void tick()
    {
        super.tick();

        //Update motion until we hit zero
        if (!this.getDeltaMovement().equals(Vec3.ZERO)) //TODO replace zero with range check to prevent rounding issues
        {
            reduceMotion();
            correctMotion();
            move(MoverType.SELF, getDeltaMovement());
        }

        //Run only if server
        if(!level().isClientSide())
        {
            redmatterLogic.tick();
        }
    }

    //<editor-fold desc="motion handling">
    private void reduceMotion()
    {
        this.setDeltaMovement(this.getDeltaMovement().scale(SPEED_REDUCTION));
    }

    private void correctMotion()
    {
        //TODO see if we can remove the sqrt and if the limit should be in an if-statement

        //Normalize motion as a speed value
        final Vec3 motion = this.getDeltaMovement();
        final float speed = (float) motion.length();

        // PORT: 1.12 divided by the speed unguarded, once the decaying motion underflowed the float speed to 0
        // the motion became NaN (0 * Infinity) and the entity moved to a NaN position. Stop the motion instead.
        if (!(speed > 1.0E-6f))
        {
            this.setDeltaMovement(Vec3.ZERO);
            return;
        }

        //Limit our velocity vector by the updated speed
        final float limitedSpeed = Math.min(speed, MAX_SPEED);
        this.setDeltaMovement(motion.scale(1.0 / speed).scale(limitedSpeed));
    }
    //</editor-fold>

    //<editor-fold desc="saving">
    @Override
    protected void readAdditionalSaveData(CompoundTag nbt)
    {
        if(nbt.contains(NBT_BLAST_SIZE))
        {
            setBlastSize(nbt.getFloat(NBT_BLAST_SIZE));
        }
        if(nbt.contains(NBT_BLAST_SIZE_MAX))
        {
            // PORT: 1.12 loaded the max size into the current size (setBlastSize), which made every redmatter
            // jump to its max size on chunk reload. Fixed to restore the max size.
            setBlastMaxSize(nbt.getFloat(NBT_BLAST_SIZE_MAX));
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag nbt)
    {
        nbt.putFloat(NBT_BLAST_SIZE, getBlastSize());
        nbt.putFloat(NBT_BLAST_SIZE_MAX, getBlastMaxSize());
    }
    //</editor-fold>

    //<editor-fold desc="disabled-props">
    @Override
    protected Entity.MovementEmission getMovementEmission()
    {
        return Entity.MovementEmission.NONE;
    }

    @Override
    public boolean isPickable()
    {
        return false;
    }
    //</editor-fold>

    //<editor-fold desc="cap-system">
    @Override
    @Nullable
    @SuppressWarnings("unchecked")
    public <T> T getEntityCapability(EntityCapability<T, Void> capability)
    {
        if (capability == ICBMClassicAPI.BLAST_CAPABILITY)
        {
            return (T) blastData;
        }
        else if (capability == ICBMClassicAPI.BLAST_VELOCITY_CAPABILITY)
        {
            return (T) capRedmatterPull;
        }
        return null;
    }
    //</editor-fold>

    public float getBlastSize()
    {
        return this.entityData.get(SIZE_DATA);
    }

    public float getBlastMaxSize()
    {
        return this.entityData.get(MAX_SIZE_DATA);
    }

    public void setBlastSize(float size)
    {
        final float limitedSize = Math.max(ConfigBlast.redmatter.MIN_SIZE, size);
        this.entityData.set(SIZE_DATA, limitedSize);
    }

    public void setBlastMaxSize(float size)
    {
        this.entityData.set(MAX_SIZE_DATA, size);
    }
}
// PORTED
