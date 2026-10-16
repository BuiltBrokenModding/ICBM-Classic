package icbm.classic.content.blast;

import icbm.classic.api.explosion.IBlastTickable;
import icbm.classic.client.ICBMSounds;
import icbm.classic.content.entity.EntityLightBeam;
import icbm.classic.content.entity.flyingblock.EntityFlyingBlock;
import icbm.classic.content.entity.flyingblock.FlyingBlock;
import icbm.classic.lib.explosive.ThreadWorkBlast;
import icbm.classic.lib.thread.IThreadWork;
import icbm.classic.lib.thread.WorkerThreadManager;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

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

    private boolean hasGeneratedFlyingBlocks = false;

    private boolean hasStartedSecondThread = false;
    private boolean hasCompetedSecondThread = false;

    private boolean hasEnabledGravityForFlyingBlocks = false;

    private boolean hasPlacedBlocks = false;

    private int secondThreadTimer = 20 * 5;

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
            this.world().explode(this.exploder, location.x(), location.y(), location.z(), 4F, net.minecraft.world.level.Level.ExplosionInteraction.TNT);

            //Create beam
            this.lightBeam = new EntityLightBeam(this.world())
                    .setPosition(location)
                    .setColor(this.red, this.green, this.blue);
            this.lightBeam.beamSize = 1;
            this.lightBeam.beamGlowSize = 2;
            this.lightBeam.setTargetBeamProgress(0.1f);
            this.world().addFreshEntity(this.lightBeam);
        }

        //Start first thread if not already started
        if (!hasStartedFirstThread)
        {
            hasStartedFirstThread = true;
            this.lightBeam.setTargetBeamProgress(0.2f);

            // PORT: worker threads read the loaded chunks captured here, see ChunkSnapshot
            captureChunkSnapshot(getBlastRadius());
            WorkerThreadManager.INSTANCE.addWork(getFirstThread());
        }

        //When first thread is completed start floating blocks and ticking down to start second thread
        if (hasCompletedFirstThread && !hasStartedSecondThread)
        {
            //Spawn flying blocks
            if (!hasGeneratedFlyingBlocks)
            {
                hasGeneratedFlyingBlocks = true;
                this.lightBeam.setTargetBeamProgress(0.5f);

                //Edit blocks and queue spawning
                for (BlockPos blockPos : blocksToRemove)
                {
                    FlyingBlock.spawnFlyingBlock(this.world, blockPos,
                        (entity) -> entity.setGravity(entity.getBlockGravity() - 0.01f),
                        (e) -> world.removeBlock(blockPos, false));
                }

                blocksToRemove.clear();
            }

            //Delay second thread start
            if (secondThreadTimer-- <= 0)
            {
                this.lightBeam.setTargetBeamProgress(0.8f);
                hasStartedSecondThread = true;

                // Captured again as chunks may have loaded or unloaded since the first thread
                captureChunkSnapshot(getBlastRadius());
                WorkerThreadManager.INSTANCE.addWork(getSecondThread());
            }
        }

        if (hasCompetedSecondThread)
        {
            this.lightBeam.setTargetBeamProgress(1f);
            if (!hasEnabledGravityForFlyingBlocks)
            {
                hasEnabledGravityForFlyingBlocks = true;
                flyingBlocks.forEach(entity -> entity.setGravity(0.5f));
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

    // PORT: 1.12 wrote the results and flags from the worker thread with no synchronization, they are
    //  handed to the server thread instead so doExplode always sees the full list with the flag
    protected IThreadWork getFirstThread()
    {
        return new ThreadWorkBlast(this.toString(), (steps, edits) -> collectFlyingBlocks(edits), edits ->
            scheduleOnServerThread(() ->
            {
                releaseChunkSnapshot();
                blocksToRemove.addAll(edits);
                hasGeneratedFlyingBlocks = false;
                hasCompletedFirstThread = true;
            }));
    }

    protected IThreadWork getSecondThread()
    {
        return new ThreadWorkBlast(this.toString(), (steps, edits) -> collectBlocksToMutate(edits), edits ->
            scheduleOnServerThread(() ->
            {
                releaseChunkSnapshot();
                blocksToRemove.addAll(edits);
                hasPlacedBlocks = false;
                hasCompetedSecondThread = true;
            }));
    }

    public boolean collectFlyingBlocks(Consumer<BlockPos> edits)
    {
        collectBlocks(edits, (int) Math.max(5, getBlastRadius() / 10));
        return false;
    }

    public boolean collectBlocksToMutate(Consumer<BlockPos> edits)
    {
        collectBlocks(edits, (int) getBlastRadius());
        return false;
    }

    //TODO make generic and recycle as a generic collector
    protected void collectBlocks(Consumer<BlockPos> edits, int r)
    {
        final int radiusSQ = r * r;
        final BlockPos.MutableBlockPos blockPos = new BlockPos.MutableBlockPos();

        BlastHelpers.forEachPosInRadius(r, (x, y, z) ->
        {
            final double distanceSQ = (x * x + y * y + z * z);
            if (distanceSQ <= radiusSQ)
            {
                //Update position
                blockPos.set(location.x() + x, location.y() + y, location.z() + z);

                //Get block, PORT: worker thread, reads the chunks captured on the server thread.
                // Chunks that were not loaded read as air and are skipped instead of being loaded.
                final BlockState state = getBlockStateThreaded(blockPos);

                //Validate TODO rework to not access blockstates, instead just collect positions
                if (!state.isAir() && state.getDestroySpeed(world, blockPos) >= 0)
                {
                    edits.accept(blockPos.immutable());
                }
            }
        });
    }

    protected abstract void mutateBlocks(List<BlockPos> edits);

    @Override
    protected void onBlastCompleted()
    {
        ICBMSounds.POWER_DOWN.play(world, location.x(), location.y(), location.z(), 4.0F, 0.8F, true);

        if (this.lightBeam != null)
        {
            this.lightBeam.startDeathCycle();
            this.lightBeam.setTargetBeamProgress(0f);
        }
    }
}
// PORTED
