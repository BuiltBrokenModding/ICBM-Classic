package icbm.classic.content.blast.threaded;

import icbm.classic.content.blast.Blast;
import icbm.classic.lib.explosive.ThreadWorkBlast;
import icbm.classic.lib.thread.IThreadWork;
import icbm.classic.lib.thread.WorkerThreadManager;
import icbm.classic.lib.transform.BlockEditHandler;
import icbm.classic.lib.transform.PosDistanceSorter;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;

import java.util.Collections;
import java.util.List;

/**
 * Created by Dark(DarkGuardsman, Robert) on 10/8/2018.
 */
public abstract class BlastThreaded extends Blast
{
    private boolean hasThreadStarted = false;

    public BlastThreaded()
    {
    }

    protected IThreadWork getWorkerTask()
    {
        return new ThreadWorkBlast((steps, edits) -> doRun(steps, edits), edits -> onWorkerThreadComplete(edits));
    }

    /**
     * @param loops - current loop count
     * @param edits - list of blocks to edit
     * @return true to run another iteration
     */
    public abstract boolean doRun(int loops, List<BlockPos> edits);


    protected void onWorkerThreadComplete(List<BlockPos> edits)
    {
        if (world instanceof WorldServer)
        {
            Collections.sort(edits, new PosDistanceSorter(location, false));

            ((WorldServer) world).addScheduledTask(() -> {
                doExplode(-1); //TODO why do we call doExplode instead of like post thread run
                BlockEditHandler.queue(world, edits, blockPos -> destroyBlock(blockPos));
                onBlastCompleted();
            });
        }
    }

    @Override
    protected boolean doExplode(int callCount)
    {
        if (!hasThreadStarted)
        {
            hasThreadStarted = true;
            WorkerThreadManager.INSTANCE.addWork(getWorkerTask());
        }
        return false;
    }

    @Override
    protected void onBlastCompleted()
    {

    }

    public void destroyBlock(BlockPos pos)
    {
        IBlockState state = this.world().getBlockState(pos);
        if (!state.getBlock().isAir(state, world(), pos))
        {
            state.getBlock().onBlockExploded(this.world(), pos, this);
        }
    }

}
