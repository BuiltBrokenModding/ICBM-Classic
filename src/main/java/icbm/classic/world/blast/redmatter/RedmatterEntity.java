package icbm.classic.world.blast.redmatter;

import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.config.blast.ConfigBlast;
import icbm.classic.world.blast.redmatter.caps.BlastRedmatterWrapper;
import icbm.classic.world.blast.redmatter.caps.CapRedmatterPull;
import icbm.classic.world.blast.redmatter.logic.RedmatterLogic;
import icbm.classic.world.blast.redmatter.render.RedmatterClientLogic;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.Level;
import net.neoforged.common.capabilities.Capability;

import javax.annotation.Nullable;

/**
 * Created by Dark(DarkGuardsman, Robert) on 4/19/2020.
 */
public class RedmatterEntity extends Entity {
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

    /**
     * Actual size of the redmatter
     */
    private static final DataParameter<Float> SIZE_DATA = EntityDataManager.createKey(RedmatterEntity.class, DataSerializers.FLOAT);
    /**
     * Largest possible size of the redmatter
     */
    private static final DataParameter<Float> MAX_SIZE_DATA = EntityDataManager.createKey(RedmatterEntity.class, DataSerializers.FLOAT);

    public RedmatterEntity(EntityType<?> type, Level level) {
        super(type, level);
        this.setSize(0.98F, 0.98F);
        this.preventEntitySpawning = true;
        this.ignoreFrustumCheck = true;
        this.ticksExisted = 0;
        this.noClip = true;
    }

    @Override
    protected void entityInit() {
        this.dataManager.register(SIZE_DATA, ConfigBlast.redmatter.DEFAULT_SIZE);
        this.dataManager.register(MAX_SIZE_DATA, ConfigBlast.redmatter.MAX_SIZE);
    }

    @Override
    public void onUpdate() {
        super.onUpdate();

        //Update motion until we hit zero
        if (this.motionX != 0 || this.motionY != 0 || this.motionZ != 0) //TODO replace zero with range check to prevent rounding issues
        {
            reduceMotion();
            correctMotion();
            move(MoverType.SELF, motionX, motionY, motionZ);
        }

        //Run only if server
        if (!world.isClientSide()) {
            redmatterLogic.tick();
        }
    }

    //<editor-fold desc="motion handling">
    private void reduceMotion() {
        this.motionX *= SPEED_REDUCTION;
        this.motionY *= SPEED_REDUCTION;
        this.motionZ *= SPEED_REDUCTION;
    }

    private void correctMotion() {
        //TODO see if we can remove the sqrt and if the limit should be in an if-statement

        //Normalize motion as a speed value
        final float speed = MathHelper.sqrt(motionX * motionX + motionY * motionY + motionZ * motionZ);
        this.motionX /= speed;
        this.motionY /= speed;
        this.motionZ /= speed;

        //Limit our velocity vector by the updated speed
        final float limitedSpeed = Math.min(speed, MAX_SPEED);
        this.motionX *= limitedSpeed;
        this.motionY *= limitedSpeed;
        this.motionZ *= limitedSpeed;
    }
    //</editor-fold>

    //<editor-fold desc="saving">
    @Override
    protected void readEntityFromNBT(CompoundTag nbt) {
        if (nbt.contains(NBT_BLAST_SIZE)) {
            setBlastSize(nbt.getFloat(NBT_BLAST_SIZE));
        }
        if (nbt.contains(NBT_BLAST_SIZE_MAX)) {
            setBlastSize(nbt.getFloat(NBT_BLAST_SIZE_MAX));
        }
    }

    @Override
    protected void writeEntityToNBT(CompoundTag nbt) {
        nbt.setFloat(NBT_BLAST_SIZE, getBlastSize());
        nbt.setFloat(NBT_BLAST_SIZE_MAX, getBlastMaxSize());
    }
    //</editor-fold>

    //<editor-fold desc="disabled-props">
    @Override
    protected boolean canTriggerWalking() {
        return false;
    }

    @Override
    public boolean canBeCollidedWith() {
        return false;
    }
    //</editor-fold>

    //<editor-fold desc="cap-system">
    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable Direction facing) {
        return capability == ICBMClassicAPI.BLAST_CAPABILITY
            || capability == ICBMClassicAPI.BLAST_VELOCITY_CAPABILITY
            || super.hasCapability(capability, facing);
    }

    @Override
    @Nullable
    public <T> T getCapability(Capability<T> capability, @Nullable Direction facing) {
        if (capability == ICBMClassicAPI.BLAST_CAPABILITY) {
            return ICBMClassicAPI.BLAST_CAPABILITY.cast(blastData);
        } else if (capability == ICBMClassicAPI.BLAST_VELOCITY_CAPABILITY) {
            return ICBMClassicAPI.BLAST_VELOCITY_CAPABILITY.cast(capRedmatterPull);
        }
        return super.getCapability(capability, facing);
    }
    //</editor-fold>

    public float getBlastSize() {
        return this.dataManager.get(SIZE_DATA);
    }

    public float getBlastMaxSize() {
        return this.dataManager.get(MAX_SIZE_DATA);
    }

    public void setBlastSize(float size) {
        final float limitedSize = Math.max(ConfigBlast.redmatter.MIN_SIZE, size);
        this.dataManager.set(SIZE_DATA, limitedSize);
    }

    public void setBlastMaxSize(float size) {
        this.dataManager.set(MAX_SIZE_DATA, size);
    }
}
