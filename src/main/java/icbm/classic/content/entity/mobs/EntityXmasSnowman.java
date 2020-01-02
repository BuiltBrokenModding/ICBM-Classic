package icbm.classic.content.entity.mobs;

import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.init.Blocks;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

/**
 *
 * Created by Dark(DarkGuardsman, Robert) on 12/31/2018.
 */
public class EntityXmasSnowman extends EntityXmasSkeleton
{
    public EntityXmasSnowman(World worldIn)
    {
        super(worldIn);
        this.setSize(0.7F, 1.9F);
    }

    @Override
    protected void applyEntityAttributes()
    {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(40.0D);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.2D);
    }

    @Override
    protected int getFireDelay()
    {
        return 40;
    }

    @Override
    public void onLivingUpdate()
    {
        super.onLivingUpdate();

        if (!this.world.isRemote)
        {
            int i = MathHelper.floor(this.posX);
            int j = MathHelper.floor(this.posY);
            int k = MathHelper.floor(this.posZ);

            if (this.isWet())
            {
                this.attackEntityFrom(DamageSource.DROWN, 1.0F);
            }

            if (this.world.getBiome(new BlockPos(i, 0, k)).getTemperature(new BlockPos(i, j, k)) > 1.0F)
            {
                this.attackEntityFrom(DamageSource.ON_FIRE, 1.0F);
            }

            if (!net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(this.world, this))
            {
                return;
            }

            for (int l = 0; l < 4; ++l)
            {
                i = MathHelper.floor(this.posX + (double) ((float) (l % 2 * 2 - 1) * 0.25F));
                j = MathHelper.floor(this.posY);
                k = MathHelper.floor(this.posZ + (double) ((float) (l / 2 % 2 * 2 - 1) * 0.25F));
                BlockPos blockpos = new BlockPos(i, j, k);

                if (this.world.getBlockState(blockpos).getMaterial() == Material.AIR && this.world.getBiome(blockpos).getTemperature(blockpos) < 0.8F && Blocks.SNOW_LAYER.canPlaceBlockAt(this.world, blockpos))
                {
                    this.world.setBlockState(blockpos, Blocks.SNOW_LAYER.getDefaultState());
                }
            }
        }
    }

    @Override
    public void attackEntityWithRangedAttack(EntityLivingBase target, float distanceFactor)
    {
        final EntityXmasRPG fragment = new EntityXmasRPG(world);
        fragment.shootingEntity = this;
        fragment.setPosition(posX, posY + getEyeHeight(), posZ);

        //Get vector between target and self
        final double deltaX = target.posX - this.posX;
        final double deltaY = target.getEntityBoundingBox().minY + (double) (target.height / 2.0F) - fragment.posY;
        final double deltaZ = target.posZ - this.posZ;

        //Get distance, used to normalize vector
        final double distance = (double) MathHelper.sqrt(deltaX * deltaX + deltaZ * deltaZ);

        //Offset from shooter body
        fragment.posX += (deltaX / distance) * 0.5;
        fragment.posZ += (deltaZ / distance) * 0.5;

        //Settings
        final float randomAim = (float) (14 - this.world.getDifficulty().getId() * 4);
        final float power = 0.5F;

        //Aim arrow
        fragment.setArrowHeading(deltaX, deltaY, deltaZ, power, randomAim);

        //this.playSound(SoundEvents.ENTITY_SKELETON_SHOOT, 1.0F, 1.0F / (this.getRNG().nextFloat() * 0.4F + 0.8F));

        //Spawn
        this.world.spawnEntity(fragment);
    }

    @Override
    public float getEyeHeight()
    {
        return 1.7F;
    }
}
