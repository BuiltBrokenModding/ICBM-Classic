package icbm.classic.content.entity.mobs;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

/**
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 1/1/2019.
 */
public class EntityXmasCreeper extends EntityXmasZombie
{
    private boolean altGun = false;

    public EntityXmasCreeper(World worldIn)
    {
        super(worldIn);
        this.setSize(0.6F, 1.7F);
    }

    @Override
    public float getEyeHeight()
    {
        return 1.4F;
    }

    @Override
    public void onUpdate()
    {
        super.onUpdate();

        /*
        double x = posX + getProjectileXOffset(0, 0);
        double y = posY + getProjectileYOffset(0, 0);
        double z = posZ + getProjectileZOffset(0, 0);

        world.spawnParticle(EnumParticleTypes.FIREWORKS_SPARK, x, y, z, 0, 0, 0);

        altGun = !altGun;
        x = posX + getProjectileXOffset(0, 0);
        y = posY + getProjectileYOffset(0, 0);
        z = posZ + getProjectileZOffset(0, 0);

       world.spawnParticle(EnumParticleTypes.FIREWORKS_SPARK, x, y, z, 0, 0, 0);

        altGun = !altGun;

        double r = getFacingRotation();
        x = posX +(Math.cos(r) - Math.sin(r)) * 0.4;
        y = posY + getProjectileYOffset(0, 0);
        z = posZ + (Math.sin(r) + Math.cos(r)) * 0.4;

        world.spawnParticle(EnumParticleTypes.FIREWORKS_SPARK, x, y, z, 0, 0, 0);
        */
    }

    @Override
    public void attackEntityWithRangedAttack(EntityLivingBase target, float distanceFactor)
    {
        super.attackEntityWithRangedAttack(target, distanceFactor);
        altGun = !altGun;
        super.attackEntityWithRangedAttack(target, distanceFactor);
    }

    @Override
    protected int getDamageForGun()
    {
        return 2; //TODO change back to 3
    }

    @Override
    protected double getProjectileXOffset(EntityLivingBase target, double delta, double distance)
    {
        double r = getArmRotation();
        final double armPos = (Math.cos(r) - Math.sin(r)) * (altGun ? 0.35 : -0.35);

        r = getFacingRotation();
        final double forwardOffset = (Math.cos(r) - Math.sin(r)) * 0.5;

        return forwardOffset + armPos;
    }

    @Override
    protected double getProjectileZOffset(EntityLivingBase target, double delta, double distance)
    {
        double r = getArmRotation();
        final double armPos = (Math.sin(r) + Math.cos(r)) * (altGun ? 0.35 : -0.35);

        r = getFacingRotation();
        final double forwardOffset = (Math.sin(r) + Math.cos(r)) * 0.5;

        return forwardOffset + armPos;
    }

    @Override
    protected double getProjectileYOffset(EntityLivingBase target, double delta, double distance)
    {
        return 1;
    }

    protected double getFacingRotation()
    {
        return Math.toRadians(MathHelper.wrapDegrees(this.getRotationYawHead() + 45));
    }

    protected double getArmRotation()
    {
        return Math.toRadians(MathHelper.wrapDegrees(this.getRotationYawHead() - 45));
    }
}
