package icbm.classic.content.blast.redmatter;

import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.config.blast.ConfigBlast;
import icbm.classic.content.blast.redmatter.caps.BlastRedmatterWrapper;
import icbm.classic.content.blast.redmatter.caps.CapRedmatterPull;
import icbm.classic.content.blast.redmatter.logic.RedmatterLogic;
import icbm.classic.content.blast.redmatter.render.RedmatterClientLogic;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MoverType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.network.play.server.SSpawnObjectPacket;
import net.minecraft.util.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Created by Dark(DarkGuardsman, Robin) on 4/19/2020.
 */
public class EntityRedmatter extends Entity
{
    public static final String NBT_BLAST_SIZE = "blast_size";
    public static final String NBT_BLAST_SIZE_MAX = "blast_size_max";


    public static final float MAX_SPEED = 0.5f;
    public static final float SPEED_REDUCTION = 0.98f;

    //Acts as an API wrapper for the entity
    public final LazyOptional<BlastRedmatterWrapper> blastData = LazyOptional.of(() -> new BlastRedmatterWrapper(this));
    public final LazyOptional<CapRedmatterPull> capRedmatterPull = LazyOptional.of(() -> new CapRedmatterPull(this));

    //Handlers
    public final RedmatterClientLogic clientLogic = new RedmatterClientLogic(this);
    public final RedmatterLogic redmatterLogic = new RedmatterLogic(this);

    /** Actual size of the redmatter */
    private static final DataParameter<Float> SIZE_DATA = EntityDataManager.createKey(EntityRedmatter.class, DataSerializers.FLOAT);
    /** Largest possible size of the redmatter */
    private static final DataParameter<Float> MAX_SIZE_DATA = EntityDataManager.createKey(EntityRedmatter.class, DataSerializers.FLOAT);

    public EntityRedmatter(EntityType<EntityRedmatter> type, World world)
    {
        super(type, world);
        this.preventEntitySpawning = true;
        this.ignoreFrustumCheck = true;
        this.ticksExisted = 0;
        this.noClip = true;
    }

    @Override
    protected void  registerData()
    {
        this.dataManager.register(SIZE_DATA, ConfigBlast.redmatter.DEFAULT_SIZE);
        this.dataManager.register(MAX_SIZE_DATA, ConfigBlast.redmatter.MAX_SIZE);
    }

    @Override
    public void tick()
    {
        super.tick();

        //Update motion until we hit zero
        if (this.getMotion().lengthSquared() > 0) //TODO replace zero with range check to prevent rounding issues
        {
            reduceMotion();
            correctMotion();
            move(MoverType.SELF, this.getMotion());
        }

        //Run only if server
        if(!world.isRemote)
        {
            redmatterLogic.tick();
        }
    }

    //<editor-fold desc="motion handling">
    private void reduceMotion()
    {
        this.setMotion(this.getMotion().mul(SPEED_REDUCTION, SPEED_REDUCTION, SPEED_REDUCTION)); //TODO reduce memory churn
    }

    private void correctMotion()
    {
        //TODO see if we can remove the sqrt and if the limit should be in an if-statement

        //Normalize motion as a speed value
        this.setMotion(this.getMotion().normalize()); //TODO reduce memory churn

        //Limit our velocity vector by the updated speed
        final float limitedSpeed = (float)Math.min(this.getMotion().length(), MAX_SPEED);
        this.setMotion(this.getMotion().scale(limitedSpeed));
    }
    //</editor-fold>

    //<editor-fold desc="saving">
    @Override
    protected void readAdditional(CompoundNBT nbt)
    {
        if(nbt.contains(NBT_BLAST_SIZE))
        {
            setBlastSize(nbt.getFloat(NBT_BLAST_SIZE));
        }
        if(nbt.contains(NBT_BLAST_SIZE_MAX))
        {
            setBlastSize(nbt.getFloat(NBT_BLAST_SIZE_MAX));
        }
    }

    @Override
    protected void writeAdditional(CompoundNBT nbt)
    {
        nbt.putFloat(NBT_BLAST_SIZE, getBlastSize());
        nbt.putFloat(NBT_BLAST_SIZE_MAX, getBlastMaxSize());
    }
    //</editor-fold>

    //<editor-fold desc="disabled-props">
    @Override
    protected boolean canTriggerWalking()
    {
        return false;
    }

    @Override
    public boolean canBeCollidedWith()
    {
        return false;
    }
    //</editor-fold>

    @Override
    public IPacket<?> createSpawnPacket() {
        return new SSpawnObjectPacket(this);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull final Capability<T> capability, final @Nullable Direction side)
    {
        if (capability == ICBMClassicAPI.BLAST_CAPABILITY)
        {
            return blastData.cast();
        }
        else if (capability == ICBMClassicAPI.BLAST_VELOCITY_CAPABILITY)
        {
            return capRedmatterPull.cast();
        }
        return super.getCapability(capability, side);
    }

    public float getBlastSize()
    {
        return this.dataManager.get(SIZE_DATA);
    }

    public float getBlastMaxSize()
    {
        return this.dataManager.get(MAX_SIZE_DATA);
    }

    public void setBlastSize(float size)
    {
        final float limitedSize = Math.max(ConfigBlast.redmatter.MIN_SIZE, size);
        this.dataManager.set(SIZE_DATA, limitedSize);
    }

    public void setBlastMaxSize(float size)
    {
        this.dataManager.set(MAX_SIZE_DATA, size);
    }
}
