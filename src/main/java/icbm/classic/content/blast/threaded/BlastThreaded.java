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
 *
 * Created by Dark(DarkGuardsman, Robert) on 10/8/2018.
 */
public abstract class BlastThreaded extends Blast
{
    public BlastThreaded(){}

    @Override
    protected void doRunBlast()
    {
        this.preExplode();
    }

    @Override
    protected void doPreExplode()
    {
        if (!this.world().isRemote)
        {
            WorkerThreadManager.INSTANCE.addWork(getWorkerTask());
        }
    }

    protected IThreadWork getWorkerTask()
    {
        return new ThreadWorkBlast((steps, edits) -> doRun(steps, edits), edits -> onWorkerThreadComplete(edits));
    }

    public boolean doRun(int loops, List<BlockPos> edits)
    {
        return false;
    }


    protected void onWorkerThreadComplete(List<BlockPos> edits)
    {
        if (world instanceof WorldServer)
        {
            Collections.sort(edits, new PosDistanceSorter(location, false));

            ((WorldServer) world).addScheduledTask(() -> {
                doExplode();
                BlockEditHandler.queue(world, edits, blockPos -> destroyBlock(blockPos));
                doPostExplode();
            });
        }
    }

    @Override
    protected void doExplode()
    {

    }

    @Override
    protected void doPostExplode()
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
