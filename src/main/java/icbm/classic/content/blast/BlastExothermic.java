package icbm.classic.content.blast;

import icbm.classic.config.ConfigBlast;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;

import java.util.List;

public class BlastExothermic extends BlastBeam
{
    public BlastExothermic()
    {
        this.red = 0.7f;
        this.green = 0.3f;
        this.blue = 0;
    }

    @Override
    protected void mutateBlocks(List<BlockPos> edits)
    {
        for (BlockPos targetPosition : edits)
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
    }

    @Override
    public void onBlastCompleted()
    {
        super.onBlastCompleted();

        //Change time of day
        if (ConfigBlast.ALLOW_DAY_NIGHT && world().getGameRules().getBoolean("doDaylightCycle"))
        {
            this.world().setWorldTime(18000);
        }
    }
}
