package icbm.classic.content.entity.mobs;

import net.minecraft.entity.projectile.EntityFireball;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

/**
 *
 * Created by Dark(DarkGuardsman, Robert) on 12/31/2018.
 */
public class EntityXmasRPG extends EntityFireball
{
    public EntityXmasRPG(World worldIn)
    {
        super(worldIn);
        this.setSize(0.3125F, 0.3125F);
    }

    @Override
    public void onUpdate()
    {
        super.onUpdate();

        if(!world.isRemote && ticksExisted > 100)
        {
            setDead();
        }
    }

    /**
     * Uses the provided coordinates as a heading and determines the velocity from it with the set
     * force and random variance. Args: x, y, z, force, forceVariation
     */
    public void setArrowHeading(double vecX, double vecY, double vecZ, float scale, float random)
    {
        //Normalize vector
        float mag = MathHelper.sqrt(vecX * vecX + vecY * vecY + vecZ * vecZ);
        vecX /= mag;
        vecY /= mag;
        vecZ /= mag;

        //Add random
        vecX += this.rand.nextGaussian() * 0.007499999832361937D * random;
        vecY += this.rand.nextGaussian() * 0.007499999832361937D * random;
        vecZ += this.rand.nextGaussian() * 0.007499999832361937D * random;

        //Scale to power
        vecX *= scale;
        vecY *= scale;
        vecZ *= scale;

        //Set motion
        this.motionX = vecX;
        this.motionY = vecY;
        this.motionZ = vecZ;

        this.accelerationX = vecX * 0.1f;
        this.accelerationY = vecY * 0.1f;
        this.accelerationZ = vecZ * 0.1f;

        //Update rotation
        float var10 = MathHelper.sqrt(vecX * vecX + vecZ * vecZ);
        this.prevRotationYaw = this.rotationYaw = (float) (Math.atan2(vecX, vecZ) * 180.0D / Math.PI);
        this.prevRotationPitch = this.rotationPitch = (float) (Math.atan2(vecY, var10) * 180.0D / Math.PI);
    }

    @Override
    protected void onImpact(RayTraceResult result)
    {
        if (!this.world.isRemote)
        {
            world.createExplosion(this, result.hitVec.x, result.hitVec.y, result.hitVec.z, 1, false);
            this.setDead();
        }
    }

    @Override
    public boolean canBeCollidedWith()
    {
        return false;
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount)
    {
        return false;
    }
}
