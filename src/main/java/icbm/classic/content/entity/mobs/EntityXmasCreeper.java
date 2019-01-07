package icbm.classic.content.entity.mobs;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.World;

/**
 *
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

    protected void debugProjectileSpawns()
    {
        double x = posX + getProjectileXOffset(null, 0, 0);
        double y = posY + getProjectileYOffset(null, 0, 0);
        double z = posZ + getProjectileZOffset(null, 0, 0);

        world.spawnParticle(EnumParticleTypes.FIREWORKS_SPARK, x, y, z, 0, 0, 0);

        altGun = !altGun;
        x = posX + getProjectileXOffset(null, 0, 0);
        y = posY + getProjectileYOffset(null, 0, 0);
        z = posZ + getProjectileZOffset(null, 0, 0);

        world.spawnParticle(EnumParticleTypes.FIREWORKS_SPARK, x, y, z, 0, 0, 0);

        altGun = !altGun;

        double r = getFacingRotation();
        x = posX + (Math.cos(r) - Math.sin(r)) * 0.4;
        y = posY + getProjectileYOffset(null, 0, 0);
        z = posZ + (Math.sin(r) + Math.cos(r)) * 0.4;

        world.spawnParticle(EnumParticleTypes.FIREWORKS_SPARK, x, y, z, 0, 0, 0);
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
    protected double getArmOffset()
    {
        return (altGun ? 0.35 : -0.35);
    }

    @Override
    protected double getForwardOffset()
    {
        return  0.5;
    }

    @Override
    protected double getProjectileYOffset(EntityLivingBase target, double delta, double distance)
    {
        return 1;
    }
}
