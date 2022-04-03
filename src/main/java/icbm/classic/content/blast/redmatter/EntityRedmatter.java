package icbm.classic.content.blast.redmatter;

import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.config.blast.ConfigBlast;
import icbm.classic.content.blast.redmatter.caps.BlastRedmatterWrapper;
import icbm.classic.content.blast.redmatter.caps.CapRedmatterPull;
import icbm.classic.content.blast.redmatter.logic.RedmatterLogic;
import icbm.classic.content.blast.redmatter.render.RedmatterClientLogic;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nullable;

/**
 * Created by Dark(DarkGuardsman, Robert) on 4/19/2020.
 */
public class EntityRedmatter extends Entity
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
    private static final DataParameter<Float> SIZE_DATA = EntityDataManager.createKey(EntityRedmatter.class, DataSerializers.FLOAT);
    /** Largest possible size of the redmatter */
    private static final DataParameter<Float> MAX_SIZE_DATA = EntityDataManager.createKey(EntityRedmatter.class, DataSerializers.FLOAT);

    public EntityRedmatter(World world)
    {
        super(world);
        this.setSize(0.98F, 0.98F);
        this.preventEntitySpawning = true;
        this.ignoreFrustumCheck = true;
        this.ticksExisted = 0;
        this.noClip = true;
    }

    @Override
    protected void entityInit()
    {
        this.dataManager.register(SIZE_DATA, ConfigBlast.REDMATTER.DEFAULT_SIZE);
        this.dataManager.register(MAX_SIZE_DATA, ConfigBlast.REDMATTER.MAX_SIZE);
    }

    @Override
    public void onUpdate()
    {
        //Update motion until we hit zero
        if (this.motionX != 0 || this.motionY != 0 || this.motionZ != 0) //TODO replace zero with range check to prevent rounding issues
        {
            reduceMotion();
            correctMotion();
            updateBoundsForMotion();
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
        this.motionX *= SPEED_REDUCTION;
        this.motionY *= SPEED_REDUCTION;
        this.motionZ *= SPEED_REDUCTION;
    }

    private void correctMotion()
    {
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

    private void updateBoundsForMotion()
    {
        this.setEntityBoundingBox(this.getEntityBoundingBox().offset(motionX, motionY, motionZ));

        //Reset position based on box
        this.posX = (this.getEntityBoundingBox().minX + this.getEntityBoundingBox().maxX) / 2.0D;
        this.posY = (this.getEntityBoundingBox().minY + this.getEntityBoundingBox().maxY) / 2.0D;
        this.posZ = (this.getEntityBoundingBox().minZ + this.getEntityBoundingBox().maxZ) / 2.0D;
    }
    //</editor-fold>

    //<editor-fold desc="saving">
    @Override
    protected void readEntityFromNBT(NBTTagCompound nbt)
    {
        if(nbt.hasKey(NBT_BLAST_SIZE))
        {
            setBlastSize(nbt.getFloat(NBT_BLAST_SIZE));
        }
        if(nbt.hasKey(NBT_BLAST_SIZE_MAX))
        {
            setBlastSize(nbt.getFloat(NBT_BLAST_SIZE_MAX));
        }
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound nbt)
    {
        nbt.setFloat(NBT_BLAST_SIZE, getBlastSize());
        nbt.setFloat(NBT_BLAST_SIZE_MAX, getBlastMaxSize());
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


    @Override
    public void move(MoverType type, double x, double y, double z)
    {
        //Remove default movement
    }
    //</editor-fold>

    //<editor-fold desc="cap-system">
    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing)
    {
        return capability == ICBMClassicAPI.BLAST_CAPABILITY
                || capability == ICBMClassicAPI.BLAST_VELOCITY_CAPABILITY
                || super.hasCapability(capability, facing);
    }

    @Override
    @Nullable
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing)
    {
        if (capability == ICBMClassicAPI.BLAST_CAPABILITY)
        {
            return ICBMClassicAPI.BLAST_CAPABILITY.cast(blastData);
        }
        else if (capability == ICBMClassicAPI.BLAST_VELOCITY_CAPABILITY)
        {
            return ICBMClassicAPI.BLAST_VELOCITY_CAPABILITY.cast(capRedmatterPull);
        }
        return super.getCapability(capability, facing);
    }
    //</editor-fold>

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
        final float limitedSize = Math.max(ConfigBlast.REDMATTER.MIN_SIZE, size);
        this.dataManager.set(SIZE_DATA, limitedSize);
    }

    public void setBlastMaxSize(float size)
    {
        this.dataManager.set(MAX_SIZE_DATA, size);
    }
}
