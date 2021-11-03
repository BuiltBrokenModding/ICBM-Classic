package icbm.classic.content.blast.helpers;

import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraftforge.fluids.IFluidBlock;

/**
 * Created by Dark(DarkGuardsman, Robert) on 5/23/2020.
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
    public static boolean isFluid(IBlockState blockState)
    {
        return blockState.getBlock() instanceof BlockLiquid || blockState.getBlock() instanceof IFluidBlock;
    }

    /**
     * Checks if the blocks state is a flowing fluid
     *
     * @param blockState to check
     * @return true if the block state is a flowing fluid
     */
    public static boolean isFlowingWater(IBlockState blockState)
    {
        return blockState.getBlock() instanceof BlockLiquid && blockState.getValue(BlockLiquid.LEVEL) < 7;
    }
}
