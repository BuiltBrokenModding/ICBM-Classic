package icbm.classic.content.blast;

import icbm.classic.config.blast.ConfigBlast;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.item.DirectionalPlaceContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;

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
        final double radius = this.getBlastRadius();
        final double radiusDecay = Math.max(1, radius * 0.3); //TODO config
        final double radiusEnsured = Math.max(1, radius * 0.1); //TODO config
        for (BlockPos targetPosition : edits)
        {
            final double delta_x = xi() - targetPosition.getX();
            final double delta_y = yi() - targetPosition.getY();
            final double delta_z = zi() - targetPosition.getZ();

            final double distance = Math.sqrt(delta_x * delta_x + delta_y * delta_y + delta_z * delta_z);
            final double distanceScale = 1 - (distance / radius);

            BlockState blockState = world.getBlockState(targetPosition);
            Block block = blockState.getBlock();

            //Turn fluids and liquid like blocks to air
            if (blockState.getMaterial() == Material.WATER || block == Blocks.ICE)
            {
                this.world().removeBlock(targetPosition, false);
            }

            //Closer to center the better the chance of spawning blocks
            if (distance <= radiusDecay || Math.random() < distanceScale)
            {
                //Destroy plants
                if (blockState.getMaterial() == Material.LEAVES
                        || blockState.getMaterial() == Material.ORGANIC
                        || blockState.getMaterial() == Material.PLANTS)
                {
                    if (!blockState.isReplaceable(new DirectionalPlaceContext(world, pos, Direction.DOWN, ItemStack.EMPTY, Direction.UP))
                        || Blocks.FIRE.isValidPosition(blockState, world(), targetPosition))
                    {
                        this.world().removeBlock(targetPosition, false);
                    }
                    else
                    {
                        this.world().setBlockState(targetPosition, net.minecraft.block.Blocks.FIRE.getDefaultState());
                    }
                }

                //Turn random stone into lava
                else if (blockState.getMaterial() == Material.ROCK)
                {
                    //Small chance to turn to lava
                    if (this.world().rand.nextFloat() > 0.9) //TODO add config
                    {
                        this.world().setBlockState(targetPosition, Blocks.LAVA.getDefaultState(), 3);
                    }
                    //Coin flip to turn to magma
                    else if (this.world().rand.nextBoolean()) //TODO add config
                    {
                        this.world().setBlockState(targetPosition.down(), Blocks.MAGMA_BLOCK.getDefaultState(), 3);
                    }
                    //Coin flip to turn to netherrack
                    else if (this.world().rand.nextBoolean() || distance <= radiusEnsured) //TODO add config
                    {
                        placeNetherrack(world, targetPosition);
                    }
                }

                //Sand replacement
                else if (blockState.getMaterial() == Material.SAND)
                {
                    if (this.world().rand.nextBoolean()) //TODO add config
                    {
                        this.world().setBlockState(targetPosition.down(), Blocks.SOUL_SAND.getDefaultState(), 3);
                    }
                    else
                    {
                        placeNetherrack(world, targetPosition);
                    }
                }

                //Ground replacement
                else if (blockState.isSolid() && (blockState.getMaterial() == Material.EARTH || blockState.getMaterial() == Material.ORGANIC))
                {
                    placeNetherrack(world, targetPosition);
                }

                //Randomly place fire TODO move to outside mutate so we always place fire while charging up
                if (Math.random() < distanceScale)
                {
                    tryPlaceFire(world, targetPosition.up(), false);
                }
            }
        }
    }

    private static void placeNetherrack(World world, BlockPos pos)
    {
        if (!world.setBlockState(pos, Blocks.NETHERRACK.getDefaultState(), 3))
        {
            System.out.println("Failed to place netherrack at " + pos);
        }

        //Place fire randomly above netherrack
        tryPlaceFire(world, pos.up(), true);
    }

    private static void tryPlaceFire(World world, BlockPos pos, boolean random)
    {
        if (!random || world.rand.nextBoolean())
        {
            //Place fire
            final BlockState blockState = world.getBlockState(pos);
            if (blockState.isReplaceable(new DirectionalPlaceContext(world, pos, Direction.DOWN, ItemStack.EMPTY, Direction.UP))
                && Blocks.FIRE.isValidPosition(blockState, world, pos))
            {
                world.setBlockState(pos, net.minecraft.block.Blocks.FIRE.getDefaultState(), 3);
            }
        }
    }

    @Override
    public void onBlastCompleted()
    {
        super.onBlastCompleted();

        //Change time of day
        if (ConfigBlast.ALLOW_DAY_NIGHT && world().getGameRules().getBoolean(GameRules.DO_DAYLIGHT_CYCLE))
        {
            this.world().setDayTime(18_000);
        }
    }
}
