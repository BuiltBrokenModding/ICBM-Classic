package icbm.classic.content.blast.threaded;

import icbm.classic.content.blast.Blast;
import icbm.classic.lib.explosive.ThreadWorkBlast;
import icbm.classic.lib.thread.IThreadWork;
import icbm.classic.lib.thread.WorkerThreadManager;
import icbm.classic.lib.transform.BlockEditHandler;
import icbm.classic.lib.transform.PosDistanceSorter;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;

import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

/**
 * Created by Dark(DarkGuardsman, Robin) on 10/8/2018.
 */
public abstract class BlastThreaded extends Blast
{
    private boolean hasThreadStarted = false;

    public BlastThreaded()
    {
    }

    protected IThreadWork getWorkerTask()
    {
        return new ThreadWorkBlast(this.toString(), this::doRun, this::onWorkerThreadComplete);
    }

    /**
     * @param loops - current loop count
     * @param edits - list of blocks to edit
     * @return true to run another iteration
     */
    public abstract boolean doRun(int loops, Consumer<BlockPos> edits);

    /**
     * Builds a sorter to sort all of the blocks post thread run
     *
     * @return
     */
    protected Comparator<BlockPos> buildSorter()
    {
        return new PosDistanceSorter(getPos(), false, PosDistanceSorter.Sort.SQ);
    }

    protected void onPostThreadJoinWorld()
    {
        doExplode(-1);
        onBlastCompleted();
    }

    /**
     * Called when the thread completes, is still inside of the thread when called.
     *
     * @param edits
     */
    protected void onWorkerThreadComplete(List<BlockPos> edits)
    {
        if (world instanceof ServerWorld)
        {
            //Sort distance
            edits.sort(buildSorter());

            //Schedule edits to run in the world
            ((ServerWorld) world).getServer().execute(() -> {

                if (skipQueue())
                {
                    edits.forEach(this::destroyBlock);
                }
                else
                {
                    //Queue edits
                    BlockEditHandler.queue(world, edits, this::destroyBlock);
                }

                //Notify blast we have entered world again
                onPostThreadJoinWorld();
            });
        }
    }

    protected boolean skipQueue()
    {
        return false;
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
        endBlast();
    }

    public void destroyBlock(BlockPos pos)
    {
        final BlockState state = this.world().getBlockState(pos);
        if (!state.getBlock().isAir(state, world(), pos))
        {
            state.getBlock().onBlockExploded(state, this.world(), pos, this);
        }
    }

}
