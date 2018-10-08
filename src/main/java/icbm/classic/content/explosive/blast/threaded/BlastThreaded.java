package icbm.classic.content.explosive.blast.threaded;

import icbm.classic.content.explosive.blast.Blast;
import icbm.classic.content.explosive.thread2.IThreadWork;
import icbm.classic.content.explosive.thread2.WorkerThreadManager;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Iterator;

/**
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 10/8/2018.
 */
public abstract class BlastThreaded extends Blast
{
    public BlastThreaded(World world, Entity entity, double x, double y, double z, float size)
    {
        super(world, entity, x, y, z, size);
    }

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

    protected abstract IThreadWork getWorkerTask();

    @Override
    protected void doExplode()
    {

    }

    @Override
    protected void doPostExplode()
    {

    }

    public void destroyBlocks(Iterable<BlockPos> edits)
    {
        //Place blocks
        Iterator<BlockPos> it = edits.iterator();
        while (it.hasNext())
        {
            destroyBlock(it.next());
        }
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
