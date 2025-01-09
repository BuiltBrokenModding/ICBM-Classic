package icbm.classic.api.actions.cause;

import net.minecraft.block.BlockState;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

/**
 * Cause containing block information
 */
public interface ICausedByBlock extends IActionCause {
    ResourceLocation getDimensionKey();

    BlockPos getBlockPos();

    BlockState getBlockState();
}
