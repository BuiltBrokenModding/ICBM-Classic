package icbm.classic.content.blast;

import icbm.classic.api.explosion.IBlastTickable;
import icbm.classic.client.ICBMSounds;
import icbm.classic.content.entity.EntityFlyingBlock;
import icbm.classic.content.entity.EntityLightBeam;
import icbm.classic.lib.explosive.ThreadWorkBlast;
import icbm.classic.lib.thread.IThreadWork;
import icbm.classic.lib.thread.WorkerThreadManager;
import icbm.classic.lib.transform.vector.Location;
import icbm.classic.lib.transform.vector.Pos;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Used by Exothermic and Endothermic explosions.
 *
 * @author Calclavia
 */
public abstract class BlastBeam extends Blast implements IBlastTickable
{
    protected Set<EntityFlyingBlock> flyingBLocks = new HashSet<EntityFlyingBlock>();
    protected EntityLightBeam lightBeam;
    protected float red, green, blue;

    private boolean firstThread = true;
    private boolean secondThread = true;
    private int secondThreadTimer = 20 * 20;

    public BlastBeam()
    {
    }

    @Override
    protected void doRunBlast()
    {

    }

    @Override
    public boolean spawnExplosiveEntity()
    {
        return true;
    }

    @Override
    protected void doPreExplode()
    {
        if (!this.world().isRemote)
        {
            ICBMSounds.BEAM_CHARGING.play(world, location.x(), location.y(), location.z(), 4.0F, 0.8F, true);

            //Basic explsoion
            //TODO remove basic in favor of thread
            this.world().createExplosion(this.exploder, location.x(), location.y(), location.z(), 4F, true);

            //Run first thread
            WorkerThreadManager.INSTANCE.addWork(getFirstThread());

            //Create beam
            this.lightBeam = new EntityLightBeam(this.world(), location, this.red, this.green, this.blue);
            this.world().spawnEntity(this.lightBeam);
        }
    }

    protected IThreadWork getFirstThread()
    {
        return new ThreadWorkBlast((steps, edits) -> collectFlyingBlocks(edits), edits -> {
            makeBlocksFly(edits);
            firstThread = false;
        });
    }

    protected IThreadWork getSecondThread()
    {
        return new ThreadWorkBlast((steps, edits) -> collectBlocksToMutate(edits), edits -> {
            mutateBlocks(edits);
            secondThread = false;
        });
    }

    public boolean collectFlyingBlocks(List<BlockPos> edits)
    {
        collectBlocks(edits, (int) Math.max(1, getBlastRadius() / 10));
        return false;
    }

    protected void makeBlocksFly(final List<BlockPos> edits)
    {
        final List<Entity> spawnList = new ArrayList(edits.size());

        //Edit blocks and queue spawning
        for (BlockPos blockPos : edits)
        {
            //Remove block
            if (world.setBlockToAir(blockPos))
            //TODO add event to cancel flying blocks
            //TODO add config to disable flying block generation
            {
                final IBlockState state = world.getBlockState(blockPos); //TODO filter what can turn into a flying block

                //Create an spawn
                final EntityFlyingBlock entity = new EntityFlyingBlock(this.world(), blockPos, state);
                entity.pitchChange = 50 * this.world().rand.nextFloat(); //TODO unmagic number
                spawnList.add(entity);
            }
        }

        //Spawn entities at the start of the next tick
        ((WorldServer) world).addScheduledTask(() -> {
            spawnList.forEach(entity -> {
                if (this.world().spawnEntity(entity))
                {
                    //Track
                    this.flyingBLocks.add((EntityFlyingBlock) entity);
                }
            });
        });
    }

    public boolean collectBlocksToMutate(List<BlockPos> edits)
    {
        collectBlocks(edits, (int) getBlastRadius());
        return false;
    }

    //TODO make generic and recycle as a generic collector
    protected void collectBlocks(List<BlockPos> edits, int r)
    {
        final int rs = r * r;
        final BlockPos.MutableBlockPos blockPos = new BlockPos.MutableBlockPos();

        for (int x = -r; x < r; x++)
        {
            for (int y = -r; y < r; y++)
            {
                for (int z = -r; z < r; z++)
                {
                    final double dist = (x * x + y * y + z * z);
                    if (dist <= rs && this.world().rand.nextInt(2) > 0)
                    {
                        //Update position
                        blockPos.setPos(location.x() + x, location.y() + y, location.z() + z);

                        //Get block
                        final IBlockState state = world.getBlockState(blockPos);
                        final Block block = state.getBlock();

                        //Validate
                        if (!block.isAir(state, world, blockPos) && state.getBlockHardness(world, blockPos) >= 0)
                        {
                            edits.add(blockPos.toImmutable());
                        }
                    }
                }
            }
        }
    }

    protected abstract void mutateBlocks(List<BlockPos> edits);

    @Override
    public boolean onBlastTick(int ticksExisted)
    {
        if (!this.world().isRemote)
        {
            if (!firstThread && secondThread && secondThreadTimer-- <= 0)
            {
                //Run second thread
                WorkerThreadManager.INSTANCE.addWork(getSecondThread());
            }
            for (EntityFlyingBlock entity : this.flyingBLocks)
            {
                Pos entityPosition = new Pos(entity);
                Pos centeredPosition = entityPosition.add(this.location.multiply(-1));
                centeredPosition.rotate(2);
                Location newPosition = this.location.add(centeredPosition);
                entity.motionX /= 3;
                entity.motionY /= 3;
                entity.motionZ /= 3;
                entity.addVelocity((newPosition.x() - entityPosition.x()) * 0.5 * this.proceduralInterval(), 0.09 * this.proceduralInterval(), (newPosition.z() - entityPosition.z()) * 0.5 * this.proceduralInterval());
                entity.yawChange += 3 * this.world().rand.nextFloat();
            }

            //End blast
            if (!isAlive || !firstThread && !secondThread)
            {
                doPostExplode();
                return true;
            }
        }
        return false;
    }

    @Override
    public void doExplode()
    {

    }

    @Override
    protected void doPostExplode()
    {
        ICBMSounds.POWER_DOWN.play(world, location.x(), location.y(), location.z(), 4.0F, 0.8F, true);

        if (this.lightBeam != null)
        {
            this.lightBeam.setDead();
            this.lightBeam = null;
        }
    }
}
