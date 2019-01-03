package icbm.classic.content.explosive.blast;

import icbm.classic.lib.transform.vector.Location;
import icbm.classic.client.ICBMSounds;
import icbm.classic.content.potion.CustomPotionEffect;
import icbm.classic.content.potion.PoisonFrostBite;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSnow;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.init.Blocks;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public class BlastEndothermic extends BlastBeam
{
    public BlastEndothermic()
    {
        this.red = 0f;
        this.green = 0.3f;
        this.blue = 0.7f;
    }

    @Override
    public void doExplode()
    {
        super.doExplode();
        ICBMSounds.REDMATTER.play(world, location.x(), location.y(), location.z(), 4.0F, 0.8F, true);
    }

    @Override
    public void doPostExplode()
    {
        super.doPostExplode();

        if (!this.world().isRemote)
        {
            if (this.canFocusBeam(this.world(), location) &&  isThreadCompleted())
            {
                /*
                 * Freeze all nearby entities.
                 */
                List<EntityLiving> livingEntities = world().getEntitiesWithinAABB(EntityLiving.class, new AxisAlignedBB(location.x() - getBlastRadius(), location.y() - getBlastRadius(), location.z() - getBlastRadius(), location.x() + getBlastRadius(), location.y() + getBlastRadius(), location.z() + getBlastRadius()));

                if (livingEntities != null && !livingEntities.isEmpty())
                {
                    Iterator<EntityLiving> it = livingEntities.iterator();

                    while (it.hasNext())
                    {
                        EntityLiving entity = it.next();
                        if (entity != null && entity.isEntityAlive())
                        {
                            entity.addPotionEffect(new CustomPotionEffect(PoisonFrostBite.INSTANCE, 60 * 20, 1, null));
                            entity.addPotionEffect(new PotionEffect(MobEffects.POISON, 10 * 20, 2));
                            entity.addPotionEffect(new PotionEffect(MobEffects.MINING_FATIGUE, 120 * 20, 2));
                            entity.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 120 * 20, 4));
                        }
                    }
                }

                ConcurrentLinkedQueue<BlockPos> list = getThreadResults();
                for (BlockPos targetPosition : list)
                {
                    double distanceFromCenter = location.distance(targetPosition);

                    if (distanceFromCenter > this.getBlastRadius())
                    {
                        continue;
                    }

                    /*
                     * Reduce the chance of setting blocks on fire based on distance from center.
                     */
                    double chance = this.getBlastRadius() - (Math.random() * distanceFromCenter);

                    if (chance > distanceFromCenter * 0.55)
                    {
                        /*
                         * Place down ice blocks.
                         */
                        IBlockState blockState = world.getBlockState(targetPosition);
                        Block block = blockState.getBlock();

                        if (blockState.getMaterial() == Material.WATER)
                        {
                            this.world().setBlockState(targetPosition, Blocks.ICE.getDefaultState(), 3);
                        }
                        else if (block == Blocks.FIRE || block == Blocks.FLOWING_LAVA || block == Blocks.LAVA)
                        {
                            this.world().setBlockState(targetPosition, Blocks.SNOW_LAYER.getDefaultState().withProperty(BlockSnow.LAYERS, 8), 3);
                        }
                        else
                        {
                            BlockPos bellowPos = targetPosition.down();
                            IBlockState blockState1 = world.getBlockState(bellowPos);

                            if ((block.isReplaceable(world(), targetPosition)) && blockState1.isSideSolid(world, bellowPos, EnumFacing.UP))
                            {
                                if (world().rand.nextBoolean())
                                {
                                    this.world().setBlockState(targetPosition, Blocks.ICE.getDefaultState(), 3);
                                }
                                else
                                {
                                    this.world().setBlockState(targetPosition, Blocks.SNOW_LAYER.getDefaultState().withProperty(BlockSnow.LAYERS, 1 + world.rand.nextInt(7)), 3);
                                }
                            }
                        }
                    }
                }

                ICBMSounds.REDMATTER.play(world, location.x(), location.y(), location.z(), 6.0F, (1.0F + (world().rand.nextFloat() - world().rand.nextFloat()) * 0.2F) * 1F, true);
            }
            if (!world().getGameRules().getBoolean("doDaylightCycle"))
            {
                this.world().setWorldTime(1200);
            }
        }
    }

    @Override
    public boolean canFocusBeam(World worldObj, Location position)
    {
        return true;
    }
}
