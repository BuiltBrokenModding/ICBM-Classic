package icbm.classic.world.blast;

import icbm.classic.config.blast.ConfigBlast;
import net.minecraft.block.material.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.init.Blocks;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class BlastExothermic extends BlastBeam {
    public BlastExothermic() {
        this.red = 0.7f;
        this.green = 0.3f;
        this.blue = 0;
    }

    @Override
    protected void mutateBlocks(List<BlockPos> edits) {
        final double radius = this.getBlastRadius();
        final double radiusDecay = Math.max(1, radius * 0.3); //TODO config
        final double radiusEnsured = Math.max(1, radius * 0.1); //TODO config
        for (BlockPos targetPosition : edits) {
            final double delta_x = location.xi() - targetPosition.getX();
            final double delta_y = location.yi() - targetPosition.getY();
            final double delta_z = location.zi() - targetPosition.getZ();

            final double distance = Math.sqrt(delta_x * delta_x + delta_y * delta_y + delta_z * delta_z);
            final double distanceScale = 1 - (distance / radius);

            BlockState blockState = world.getBlockState(targetPosition);
            Block block = blockState.getBlock();

            //Turn fluids and liquid like blocks to air
            if (blockState.getMaterial() == Material.WATER || block == Blocks.ICE) {
                this.level().setBlockToAir(targetPosition);
            }

            //Closer to center the better the chance of spawning blocks
            if (distance <= radiusDecay || Math.random() < distanceScale) {
                //Destroy plants
                if (blockState.getMaterial() == Material.LEAVES
                    || blockState.getMaterial() == Material.VINE
                    || blockState.getMaterial() == Material.PLANTS) {
                    if (!block.isReplaceable(level(), targetPosition) || Blocks.FIRE.canPlaceBlockAt(level(), targetPosition)) {
                        this.level().setBlockToAir(targetPosition);
                    } else {
                        this.level().setBlockState(targetPosition, Blocks.FIRE.getDefaultState());
                    }
                }

                //Turn random stone into lava
                else if (blockState.getMaterial() == Material.ROCK) {
                    //Small chance to turn to lava
                    if (this.level().rand.nextFloat() > 0.9) //TODO add config
                    {
                        this.level().setBlockState(targetPosition, Blocks.FLOWING_LAVA.getDefaultState(), 3);
                    }
                    //Coin flip to turn to magma
                    else if (this.level().rand.nextBoolean()) //TODO add config
                    {
                        this.level().setBlockState(targetPosition.below(), Blocks.MAGMA.getDefaultState(), 3);
                    }
                    //Coin flip to turn to netherrack
                    else if (this.level().rand.nextBoolean() || distance <= radiusEnsured) //TODO add config
                    {
                        placeNetherrack(world, targetPosition);
                    }
                }

                //Sand replacement
                else if (blockState.getMaterial() == Material.SAND) {
                    if (this.level().rand.nextBoolean()) //TODO add config
                    {
                        this.level().setBlockState(targetPosition.below(), Blocks.SOUL_SAND.getDefaultState(), 3);
                    } else {
                        placeNetherrack(world, targetPosition);
                    }
                }

                //Ground replacement
                else if (blockState.getMaterial() == Material.GROUND || blockState.getMaterial() == Material.GRASS) {
                    placeNetherrack(world, targetPosition);
                }

                //Randomly place fire TODO move to outside mutate so we always place fire while charging up
                if (Math.random() < distanceScale) {
                    tryPlaceFire(world, targetPosition.up(), false);
                }
            }
        }
    }

    private static void placeNetherrack(Level level, BlockPos pos) {
        if (!world.setBlockState(pos, Blocks.NETHERRACK.getDefaultState(), 3)) {
            System.out.println("Failed to place netherrack at " + pos);
        }

        //Place fire randomly above netherrack
        tryPlaceFire(world, pos.up(), true);
    }

    private static void tryPlaceFire(Level level, BlockPos pos, boolean random) {
        if (!random || world.rand.nextBoolean()) {
            //Place fire
            final BlockState blockState = world.getBlockState(pos);
            if (blockState.getBlock().isReplaceable(world, pos) && Blocks.FIRE.canPlaceBlockAt(world, pos)) {
                world.setBlockState(pos, Blocks.FIRE.getDefaultState(), 3);
            }
        }
    }

    @Override
    public void onBlastCompleted() {
        super.onBlastCompleted();

        //Change time of day
        if (ConfigBlast.ALLOW_DAY_NIGHT && level().getGameRules().getBoolean("doDaylightCycle")) {
            this.level().setWorldTime(18000);
        }
    }
}
