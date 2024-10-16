package icbm.classic.content.blast;

import icbm.classic.api.explosion.IBlastTickable;
import icbm.classic.content.blast.threaded.BlastThreaded;
import icbm.classic.content.radioactive.RadioactiveHandler;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import org.apache.commons.lang3.tuple.Pair;

import java.util.function.Consumer;

/**
 * Handles switching out blocks for dead version or with radioactive versions
 */
public class BlastRadioactiveBlockSwaps extends BlastThreaded implements IBlastTickable
{
    @Override
    public boolean doRun(int loops, Consumer<BlockPos> edits)
    {
        BlastHelpers.forEachPosInRadius(this.getBlastRadius(), (x, y, z) ->
        edits.accept(new BlockPos(xi() + x, yi() + y, zi() + z)));
        //TODO implement raytrace to reduce impact of effects behind protective shielding
        //TODO only contaminate top few layers of earth
        return false;
    }

    @Override
    public void destroyBlock(BlockPos targetPosition)
    {
        final IBlockState blockState = world.getBlockState(targetPosition);
        final Pair<IBlockState, Float> replacement = RadioactiveHandler.radioactiveBlockSwaps.getValue(blockState);
        if(replacement != null && (replacement.getValue() == null || world.rand.nextFloat() <= replacement.getValue())) {
            world.setBlockState(targetPosition, replacement.getKey(), 3);
        }
    }
}
