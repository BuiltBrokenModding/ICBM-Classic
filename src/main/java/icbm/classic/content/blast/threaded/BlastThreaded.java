package icbm.classic.content.blast.threaded;

import icbm.classic.config.ConfigBlast;
import icbm.classic.content.blast.Blast;
import icbm.classic.lib.explosive.ThreadWorkBlast;
import icbm.classic.lib.thread.IThreadWork;
import icbm.classic.lib.thread.WorkerThreadManager;
import icbm.classic.lib.transform.BlockEditHandler;
import icbm.classic.lib.transform.PosDistanceSorter;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.WorldServer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

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
    public abstract boolean doRun(int loops, Consumer<BlockPos> edits);


    protected void onWorkerThreadComplete(List<BlockPos> edits)
    {
        if (world instanceof WorldServer)
        {
            edits.sort(new PosDistanceSorter(location, false));

            ((WorldServer) world).addScheduledTask(() -> {
                doExplode(-1); //TODO why do we call doExplode instead of like post thread run
                List<BlockPos> queuedForLater = new ArrayList<>();
                BlockEditHandler.queue(world, edits, blockPos ->
                {
                    Block b = world.getBlockState(blockPos).getBlock();
                    if (ConfigBlast.BLAST_DO_BLOCKUPDATES  && b == Blocks.WATER || b == Blocks.FLOWING_WATER)
                    {
                        queuedForLater.add(blockPos);
                    }
                    destroyBlock(blockPos);
                });

                if (!queuedForLater.isEmpty()) {
                    List<BlockPos> dummy = new ArrayList<>(); // TODO clean up this dummy crap, by adding a callback ot the blockedithandler
                    dummy.add(new BlockPos(0, 0, 0));
                    BlockEditHandler.queue(world, dummy, blockPos -> {
                        Comparator<BlockPos> compareByY = Comparator.comparingInt(Vec3i::getY);
                        queuedForLater.sort(compareByY);
                        queuedForLater.sort(Collections.reverseOrder());
                        BlockEditHandler.queue(world, queuedForLater, (blockPos1 ->
                        {
                            world.setBlockState(blockPos1, Blocks.AIR.getDefaultState(), 3);
                        }
                        ));
                    });
                }
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
