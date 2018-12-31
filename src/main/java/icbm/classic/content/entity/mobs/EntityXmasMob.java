package icbm.classic.content.entity.mobs;

import icbm.classic.content.entity.EntityFragments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
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

    public abstract boolean isOnTeam(Entity entity);

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
        final EntityFragments fragment = new EntityFragments(world, posX, posY + getEyeHeight(), posZ, false, false);
        fragment.shootingEntity = this;
        fragment.isIce = true;
        fragment.isXmasBullet = true;
        fragment.damage = 3;

        //Get vector between target and self
        final double deltaX = target.posX - this.posX;
        final double deltaY = target.getEntityBoundingBox().minY + (double) (target.height / 2.0F) - fragment.posY;
        final double deltaZ = target.posZ - this.posZ;

        //Get distance, used to normalize vector
        final double distance = (double) MathHelper.sqrt(deltaX * deltaX + deltaZ * deltaZ);

        //Offset from shooter body
        fragment.posX += (deltaX / distance) * 0.3;
        fragment.posZ += (deltaZ / distance) * 0.3;

        //Settings
        final float randomAim = (float) (14 - this.world.getDifficulty().getId() * 4);
        final float power = 3F;

        //Aim arrow
        fragment.setArrowHeading(deltaX, deltaY, deltaZ, power, randomAim);

        //this.playSound(SoundEvents.ENTITY_SKELETON_SHOOT, 1.0F, 1.0F / (this.getRNG().nextFloat() * 0.4F + 0.8F));

        //Spawn
        this.world.spawnEntity(fragment);
    }

    @Override
    public void setSwingingArms(boolean swingingArms)
    {

    }
}
