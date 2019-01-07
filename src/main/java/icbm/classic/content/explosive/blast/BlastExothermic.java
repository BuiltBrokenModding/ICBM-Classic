package icbm.classic.content.explosive.blast;

import icbm.classic.config.ConfigBlast;
import icbm.classic.lib.transform.vector.Location;
import icbm.classic.client.ICBMSounds;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.concurrent.ConcurrentLinkedQueue;

public class BlastExothermic extends BlastBeam
{
    public BlastExothermic()
    {
        this.red = 0.7f;
        this.green = 0.3f;
        this.blue = 0;
    }

    @Override
    public void doExplode()
    {
        super.doExplode();
        ICBMSounds.BEAM_CHARGING.play(world, location.x(), location.y(), location.z(), 4.0F, 0.8F, true);
    }

    @Override
    public void doPostExplode()
    {
        super.doPostExplode();

        if (!this.world().isRemote)
        {
            ICBMSounds.POWER_DOWN.play(world, location.x(), location.y(), location.z(), 4.0F, 0.8F, true);

            if (this.canFocusBeam(this.world(), location) && isThreadCompleted())
            {
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
                         * Check to see if the block is an air block and there is a block below it
                         * to support the fire.
                         */
                        IBlockState blockState = world.getBlockState(targetPosition);
                        Block block = blockState.getBlock();

                        if (blockState.getMaterial() == Material.WATER || block == Blocks.ICE)
                        {
                            this.world().setBlockToAir(targetPosition);
                        }

                        if (blockState.getMaterial() == Material.ROCK && this.world().rand.nextFloat() > 0.8)
                        {
                            this.world().setBlockState(targetPosition, Blocks.FLOWING_LAVA.getDefaultState(), 3);
                        }

                        if ((block.isReplaceable(world(), targetPosition))
                                && Blocks.FIRE.canPlaceBlockAt(world(), targetPosition))
                        {
                            if (this.world().rand.nextFloat() > 0.99)
                            {
                                this.world().setBlockState(targetPosition, Blocks.FLOWING_LAVA.getDefaultState(), 3);
                            }
                            else
                            {
                                this.world().setBlockState(targetPosition, Blocks.FIRE.getDefaultState(), 3);

                                blockState = this.world().getBlockState(targetPosition.down());
                                block = blockState.getBlock();

                                if (ConfigBlast.EXOTHERMIC_CREATE_NETHER_RACK && (block == Blocks.STONE || block == Blocks.GRASS || block == Blocks.DIRT) && this.world().rand.nextFloat() > 0.75)
                                {
                                    this.world().setBlockState(targetPosition.down(), Blocks.NETHERRACK.getDefaultState(), 3);
                                }
                            }
                        }
                    }
                }

                ICBMSounds.EXPLOSION_FIRE.play(world, location.x() + 0.5D, location.y() + 0.5D, location.z() + 0.5D, 6.0F, (1.0F + (world().rand.nextFloat() - world().rand.nextFloat()) * 0.2F) * 1F, true);
            }

            if(!world().getGameRules().getBoolean("doDaylightCycle")) //TODO add config
            {
                this.world().setWorldTime(18000);
            }
        }
    }

    @Override
    public boolean canFocusBeam(World worldObj, Location position)
    {
        return true;
    }
}
