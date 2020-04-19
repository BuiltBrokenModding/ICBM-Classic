package icbm.classic.content.blast.redmatter;

import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

/**
 * Created by Dark(DarkGuardsman, Robert) on 4/19/2020.
 */
public class EntityRedmatter extends Entity
{
    private final BlastRedmatter blast = new BlastRedmatter();

    public EntityRedmatter(World world)
    {
        super(world);
        this.preventEntitySpawning = true;
        this.noClip = true;
        this.setSize(0.98F, 0.98F);
        this.ignoreFrustumCheck = true;
        this.ticksExisted = 0;
    }

    @Override
    protected void entityInit()
    {
        blast.setBlastSource(this);
    }

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
    public void onUpdate()
    {
       if(this.motionX != 0 || this.motionY != 0 || this.motionZ != 0) {
           reduceMotion();
           correctMotion();
           updateBoundsForMotion();
       }
    }

    private void reduceMotion() {
        //Slow entity down
        this.motionX *= .98;
        this.motionY *= .98;
        this.motionZ *= .98;
    }

    private void correctMotion() {
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

    private void updateBoundsForMotion() {
        this.setEntityBoundingBox(this.getEntityBoundingBox().offset(motionX, motionY, motionZ));

        //Reset position based on box
        this.posX = (this.getEntityBoundingBox().minX + this.getEntityBoundingBox().maxX) / 2.0D;
        this.posY = (this.getEntityBoundingBox().minY + this.getEntityBoundingBox().maxY) / 2.0D;
        this.posZ = (this.getEntityBoundingBox().minZ + this.getEntityBoundingBox().maxZ) / 2.0D;
    }

    @Override
    public void move(MoverType type, double x, double y, double z)
    {
        //Remove default movement
    }


    @Override
    protected void readEntityFromNBT(NBTTagCompound nbt)
    {
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound nbt)
    {

    }
}
