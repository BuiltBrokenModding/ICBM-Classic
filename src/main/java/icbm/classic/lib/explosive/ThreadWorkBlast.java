package icbm.classic.lib.explosive;

import icbm.classic.ICBMClassic;
import icbm.classic.lib.thread.IThreadWork;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;

/**
 *
 * Created by Dark(DarkGuardsman, Robert) on 10/8/2018.
 */
public class ThreadWorkBlast implements IThreadWork
{
    public List<BlockPos> editPositions = new ArrayList();
    public BiFunction<Integer, Consumer<BlockPos>, Boolean> runFunction;
    public Consumer<List<BlockPos>> onComplete;

    public ThreadWorkBlast(BiFunction<Integer, Consumer<BlockPos>, Boolean> runFunction, Consumer<List<BlockPos>> onComplete)
    {
        this.runFunction = runFunction;
        this.onComplete = onComplete;
    }

    @Override
    public boolean doRun(int steps)
    {
        return runFunction.apply(steps, (blockPos) -> editPositions.add(blockPos));
    }

    @Override
    public void onStarted()
    {
        ICBMClassic.logger().debug(toString() + " started");
    }

    @Override
    public void onCompleted()
    {
        ICBMClassic.logger().debug(toString() + " completed");
        onComplete.accept(editPositions);
    }
}
