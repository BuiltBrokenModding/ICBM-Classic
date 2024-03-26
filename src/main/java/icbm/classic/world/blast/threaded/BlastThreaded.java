package icbm.classic.world.blast.threaded;

import icbm.classic.lib.explosive.ThreadWorkBlast;
import icbm.classic.lib.thread.IThreadWork;
import icbm.classic.lib.thread.WorkerThreadManager;
import icbm.classic.lib.transform.BlockEditHandler;
import icbm.classic.lib.transform.PosDistanceSorter;
import icbm.classic.world.blast.Blast;
import net.minecraft.core.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

/**
 * Created by Dark(DarkGuardsman, Robert) on 10/8/2018.
 */
public abstract class BlastThreaded extends Blast {
    private boolean hasThreadStarted = false;

    public BlastThreaded() {
    }

    protected IThreadWork getWorkerTask() {
        return new ThreadWorkBlast(this::doRun, this::onWorkerThreadComplete);
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
    protected Comparator<BlockPos> buildSorter() {
        return new PosDistanceSorter(location, false, PosDistanceSorter.Sort.SQ);
    }

    protected void onPostThreadJoinLevel() {
        doExplode(-1);
        onBlastCompleted();
    }

    /**
     * Called when the thread completes, is still inside of the thread when called.
     *
     * @param edits
     */
    protected void onWorkerThreadComplete(List<BlockPos> edits) {
        if (world instanceof WorldServer) {
            //Sort distance
            edits.sort(buildSorter());

            //Schedule edits to run in the world
            ((WorldServer) world).addScheduledTask(() -> {

                if (skipQueue()) {
                    edits.forEach(blockPos -> destroyBlock(blockPos));
                } else {
                    //Queue edits
                    BlockEditHandler.queue(world, edits, blockPos -> destroyBlock(blockPos));
                }

                //Notify blast we have entered world again
                onPostThreadJoinLevel();
            });
        }
    }

    protected boolean skipQueue() {
        return false;
    }

    @Override
    protected boolean doExplode(int callCount) {
        if (!hasThreadStarted) {
            hasThreadStarted = true;
            WorkerThreadManager.INSTANCE.addWork(getWorkerTask());
        }
        return false;
    }

    @Override
    protected void onBlastCompleted() {

    }

    public void destroyBlock(BlockPos pos) {
        BlockState state = this.level().getBlockState(pos);
        if (!state.getBlock().isAir(state, level(), pos)) {
            state.getBlock().onBlockExploded(this.level(), pos, this);
        }
    }

}