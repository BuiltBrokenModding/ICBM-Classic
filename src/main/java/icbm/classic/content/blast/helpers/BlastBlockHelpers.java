package icbm.classic.content.blast.helpers;

import net.minecraft.block.BlockState;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraftforge.fluids.IFluidBlock;

/**
 * Created by Dark(DarkGuardsman, Robin) on 5/23/2020.
 */
public final class BlastBlockHelpers
{
    private BlastBlockHelpers()
    {
        //Private and empty to prevent creation
    }

    /**
     * Checks if a block state is a fluid
     *
     * @param blockState to check
     * @return true if the block state is a fluid
     */
    public static boolean isFluid(BlockState blockState)
    {
        return blockState.getBlock() instanceof FlowingFluidBlock || blockState.getBlock() instanceof IFluidBlock;
    }

    /**
     * Checks if the blocks state is a flowing fluid
     *
     * @param blockState to check
     * @return true if the block state is a flowing fluid
     */
    public static boolean isFlowingWater(BlockState blockState)
    {
        return blockState.getBlock() instanceof FlowingFluidBlock && blockState.get(FlowingFluidBlock.LEVEL) < 7;
    }
}
