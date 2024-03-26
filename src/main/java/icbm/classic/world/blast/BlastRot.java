package icbm.classic.world.blast;

import icbm.classic.ICBMClassic;
import icbm.classic.api.explosion.IBlastTickable;
import icbm.classic.world.blast.threaded.BlastThreaded;
import net.minecraft.block.material.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.init.Blocks;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.fluids.FluidRegistry;

import java.util.function.Consumer;

/**
 * Creates radiation spawning
 *
 * @author Calclavia
 */
public class BlastRot extends BlastThreaded implements IBlastTickable {
    @Override
    public boolean doRun(int loops, Consumer<BlockPos> edits) {
        BlastHelpers.forEachPosInRadius(this.getBlastRadius(), (x, y, z) ->
            edits.accept(new BlockPos(xi() + x, yi() + y, zi() + z)));
        //TODO implement pathfinder so virus doesn't go through unbreakable air-tight walls
        return false;
    }

    @Override
    public void destroyBlock(BlockPos targetPosition) {
        //get block
        final BlockState blockState = world.getBlockState(targetPosition);
        final Block block = blockState.getBlock();

        if (block == Blocks.GRASS || block == Blocks.SAND) {
            if (this.level().rand.nextFloat() > 0.96) {
                world.setBlockState(targetPosition, ICBMClassic.blockRadioactive.getDefaultState(), 3);
            }
        }

        if (block == Blocks.STONE) {
            if (this.level().rand.nextFloat() > 0.99) {
                world.setBlockState(targetPosition, ICBMClassic.blockRadioactive.getDefaultState(), 3);
            }
        } else if (blockState.getMaterial() == Material.LEAVES || blockState.getMaterial() == Material.PLANTS) {
            world.setBlockToAir(targetPosition);
        } else if (block == Blocks.FARMLAND) {
            world.setBlockState(targetPosition, ICBMClassic.blockRadioactive.getDefaultState(), 3);
        } else if (blockState.getMaterial() == Material.WATER) {
            if (FluidRegistry.getFluid("toxicwaste") != null) {
                Block blockToxic = FluidRegistry.getFluid("toxicwaste").getBlock();
                if (blockToxic != null) {
                    world.setBlockState(targetPosition, blockToxic.getDefaultState(), 3);
                }
            }
        }
    }
}
