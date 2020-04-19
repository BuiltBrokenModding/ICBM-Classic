package icbm.classic.content.blast.redmatter;

import icbm.classic.api.ICBMClassicAPI;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.nbt.NBTTagCompound;
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
    //Acts as an API wrapper for the entity
    private final BlastRedmatterWrapper blastData = new BlastRedmatterWrapper(this);

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

    }

    @Override
    public void onUpdate()
    {
        if (this.motionX != 0 || this.motionY != 0 || this.motionZ != 0)
        {
            reduceMotion();
            correctMotion();
            updateBoundsForMotion();
        }
    }

    //<editor-fold desc="motion">
    private void reduceMotion()
    {
        //Slow entity down
        this.motionX *= .98;
        this.motionY *= .98;
        this.motionZ *= .98;
    }

    private void correctMotion()
    {
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
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound nbt)
    {

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
        return super.getCapability(capability, facing);
    }
    //</editor-fold>
}
