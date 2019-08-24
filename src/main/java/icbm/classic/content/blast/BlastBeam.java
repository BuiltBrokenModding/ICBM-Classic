package icbm.classic.content.blast;

import icbm.classic.api.explosion.IBlastTickable;
import icbm.classic.client.ICBMSounds;
import icbm.classic.content.entity.EntityFlyingBlock;
import icbm.classic.content.entity.EntityLightBeam;
import icbm.classic.lib.explosive.ThreadWorkBlast;
import icbm.classic.lib.thread.IThreadWork;
import icbm.classic.lib.thread.WorkerThreadManager;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;

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
    protected final Set<EntityFlyingBlock> flyingBlocks = new HashSet();
    protected final List<BlockPos> blocksToRemove = new ArrayList();

    protected EntityLightBeam lightBeam;
    protected float red, green, blue;

    private boolean hasDoneSetup = false;

    private boolean hasStartedFirstThread = false;
    private boolean hasCompletedFirstThread = false;

    private boolean hasGeneratedFlyingBLocks = false;

    private boolean hasStartedSecondThread = false;
    private boolean hasCompetedSecondThread = false;

    private boolean hasEnabledGravityForFlyingBlocks = false;

    private boolean hasPlacedBlocks = false;

    private int secondThreadTimer = 20 * 20;

    public BlastBeam()
    {
    }

    @Override
    protected boolean doExplode(int callCount)
    {
        if (!hasDoneSetup)
        {
            hasDoneSetup = true;

            //Play audio
            ICBMSounds.BEAM_CHARGING.play(world, location.x(), location.y(), location.z(), 4.0F, 0.8F, true);

            //Basic explosion
            //TODO remove basic in favor of thread
            this.world().createExplosion(this.exploder, location.x(), location.y(), location.z(), 4F, true);

            //Create beam
            this.lightBeam = new EntityLightBeam(this.world(), location, this.red, this.green, this.blue);
            this.world().spawnEntity(this.lightBeam);
        }

        //Start first thread if not already started
        if (!hasStartedFirstThread)
        {
            hasStartedFirstThread = true;
            WorkerThreadManager.INSTANCE.addWork(getFirstThread());
        }

        //When first thread is completed start floating blocks and ticking down to start second thread
        if (hasCompletedFirstThread && !hasStartedSecondThread)
        {
            //Spawn flying blocks
            if (!hasGeneratedFlyingBLocks)
            {
                hasGeneratedFlyingBLocks = true;

                //Edit blocks and queue spawning
                for (BlockPos blockPos : blocksToRemove)
                {
                    final IBlockState state = world.getBlockState(blockPos); //TODO filter what can turn into a flying block

                    //Remove block
                    if (world.setBlockToAir(blockPos))
                    {
                        //Create an spawn
                        final EntityFlyingBlock entity = new EntityFlyingBlock(this.world(), blockPos, state);
                        entity.gravity = -entity.gravity;
                        if (world.spawnEntity(entity))
                        {
                            flyingBlocks.add(entity);
                        }
                    }
                }

                blocksToRemove.clear();
            }

            //Delay second thread start
            if (secondThreadTimer-- <= 0)
            {
                hasStartedSecondThread = true;
                WorkerThreadManager.INSTANCE.addWork(getSecondThread());
            }
        }

        if (hasCompetedSecondThread)
        {
            if (!hasEnabledGravityForFlyingBlocks)
            {
                hasEnabledGravityForFlyingBlocks = true;
                flyingBlocks.forEach(entity -> entity.gravity = Math.abs(entity.gravity));
            }

            if (!hasPlacedBlocks)
            {
                hasPlacedBlocks = true;
                mutateBlocks(blocksToRemove);
                blocksToRemove.clear();
            }
        }
        return hasPlacedBlocks;
    }

    protected IThreadWork getFirstThread()
    {
        return new ThreadWorkBlast((steps, edits) -> collectFlyingBlocks(edits), edits -> {

            blocksToRemove.addAll(edits);
            hasGeneratedFlyingBLocks = false;
            hasCompletedFirstThread = true;
        });
    }

    protected IThreadWork getSecondThread()
    {
        return new ThreadWorkBlast((steps, edits) -> collectBlocksToMutate(edits), edits -> {
            blocksToRemove.addAll(edits);
            hasPlacedBlocks = false;
            hasCompetedSecondThread = true;
        });
    }

    public boolean collectFlyingBlocks(List<BlockPos> edits)
    {
        collectBlocks(edits, (int) Math.max(1, getBlastRadius() / 10));
        return false;
    }

    public boolean collectBlocksToMutate(List<BlockPos> edits)
    {
        collectBlocks(edits, (int) getBlastRadius() / 4);
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
    protected void onBlastCompleted()
    {
        ICBMSounds.POWER_DOWN.play(world, location.x(), location.y(), location.z(), 4.0F, 0.8F, true);

        if (this.lightBeam != null)
        {
            this.lightBeam.setDead();
            this.lightBeam = null;
        }
    }
}
