package icbm.classic.content.entity.mobs;

import icbm.classic.content.entity.EntityFragments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

/**
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 12/31/2018.
 */
public abstract class EntityXmasMob extends EntityMob implements IRangedAttackMob
{
    public EntityXmasMob(World worldIn)
    {
        super(worldIn);
        this.setSize(0.6F, 1.6F);
    }

    @Override
    protected void initEntityAI()
    {
        this.tasks.addTask(1, new EntityAISwimming(this));
        this.tasks.addTask(4, new EntityAIAttackRanged(this, 0.1D, getFireDelay(), 50.0F));
        this.tasks.addTask(5, new EntityAIWanderAvoidWater(this, 1.0D));
        this.tasks.addTask(6, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
        this.tasks.addTask(6, new EntityAILookIdle(this));

        this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, false, new Class[0]));
    }

    protected int getFireDelay()
    {
        return 5;
    }

    public boolean isOnTeam(Entity entity)
    {
        if(entity instanceof EntityXmasMob)
        {
            return ((EntityXmasMob) entity).isIceFaction() == isIceFaction();
        }
        return false;
    }

    public abstract boolean isIceFaction();

    @Override
    protected void applyEntityAttributes()
    {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(50);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.25D);
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(12);
    }

    @Override
    public void attackEntityWithRangedAttack(EntityLivingBase target, float distanceFactor)
    {
        final EntityFragments fragment = getProjectile(target, distanceFactor);
        fragment.shootingEntity = this;
        fragment.isIce = isIceFaction();
        fragment.isFire = !isIceFaction();
        fragment.isXmasBullet = true;
        fragment.damage = getDamageForGun();

        //Get different in distance
        double deltaX = target.posX - this.posX;
        double deltaY = target.getEntityBoundingBox().minY + (double) (target.height / 3.0F) - fragment.posY;
        double deltaZ = target.posZ - this.posZ;
        double distance = (double) MathHelper.sqrt(deltaX * deltaX + deltaZ * deltaZ);

        //Offset from shooter body
        fragment.posX += getProjectileXOffset(target, deltaX, distance);
        fragment.posY += getProjectileYOffset(target, deltaY, distance); //TODO turn all 3 offsets into a single method that uses a POS object
        fragment.posZ += getProjectileZOffset(target, deltaZ, distance);

        //Get vector between target and self, doing it again as offset changes the values
        deltaX = target.posX - this.posX;
        deltaY = target.getEntityBoundingBox().minY + (double) (target.height / 3.0F) - fragment.posY;
        deltaZ = target.posZ - this.posZ;

        //Settings
        final float randomAim = getProjectileRandom();
        final float power = getProjectilePower();

        //Aim arrow
        fragment.setArrowHeading(deltaX, deltaY, deltaZ, power, randomAim);

        //this.playSound(SoundEvents.ENTITY_SKELETON_SHOOT, 1.0F, 1.0F / (this.getRNG().nextFloat() * 0.4F + 0.8F));

        //Spawn
        this.world.spawnEntity(fragment);
    }

    protected float getProjectilePower()
    {
        return 2F;
    }

    protected float getProjectileRandom()
    {
        return (float) (14 - this.world.getDifficulty().getId() * 4);
    }

    protected int getDamageForGun()
    {
        return 3;
    }

    protected double getProjectileYOffset(EntityLivingBase target, double deltaY, double distance)
    {
        return getEyeHeight();
    }

    protected double getProjectileXOffset(EntityLivingBase target, double delta, double distance)
    {
        double r = getArmRotation();
        final double armPos = (Math.cos(r) - Math.sin(r)) * getArmOffset();

        r = getFacingRotation();
        final double forwardOffset = (Math.cos(r) - Math.sin(r)) * getForwardOffset();

        return forwardOffset + armPos;
    }


    protected double getProjectileZOffset(EntityLivingBase target, double delta, double distance)
    {
        double r = getArmRotation();
        final double armPos = (Math.sin(r) + Math.cos(r)) * getArmOffset();

        r = getFacingRotation();
        final double forwardOffset = (Math.sin(r) + Math.cos(r)) * getForwardOffset();

        return forwardOffset + armPos;
    }

    protected double getArmOffset()
    {
        return -0.35;
    }

    protected double getForwardOffset()
    {
        return  0.5;
    }

    protected double getFacingRotation()
    {
        return Math.toRadians(MathHelper.wrapDegrees(this.getRotationYawHead() + 45));
    }

    protected double getArmRotation()
    {
        return Math.toRadians(MathHelper.wrapDegrees(this.getRotationYawHead() - 45));
    }

    protected EntityFragments getProjectile(EntityLivingBase target, float distanceFactor)
    {
        return new EntityFragments(world, posX, posY, posZ, false, false);
    }

    @Override
    public void setSwingingArms(boolean swingingArms)
    {

    }

    @Override
    public void onUpdate()
    {
        super.onUpdate();
        debugProjectileSpawns();
    }

    protected void debugProjectileSpawns()
    {
        double x = posX + getProjectileXOffset(null, 0, 0);
        double y = posY + getProjectileYOffset(null, 0, 0);
        double z = posZ + getProjectileZOffset(null, 0, 0);

        world.spawnParticle(EnumParticleTypes.FIREWORKS_SPARK, x, y, z, 0, 0, 0);
    }
}
