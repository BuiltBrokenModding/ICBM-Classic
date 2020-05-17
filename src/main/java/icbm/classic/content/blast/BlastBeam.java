package icbm.classic.content.blast;

import icbm.classic.api.events.BlastBlockModifyEvent;
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
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;

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

    protected static EntityFlyingBlock destroyBlock(World world, BlockPos blockPos, IBlockState state) {
        if (world.setBlockToAir(blockPos))
        {
            //Create an spawn
            final EntityFlyingBlock entity = new EntityFlyingBlock(world, blockPos, state);
            entity.gravity = -0.01f;
            if (world.spawnEntity(entity))
            {
                return entity;
            }
        }
        return null;
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
            this.lightBeam = new EntityLightBeam(this.world())
                    .setPosition(location)
                    .setColor(this.red, this.green, this.blue);
            this.lightBeam.beamSize = 1;
            this.lightBeam.beamGlowSize = 2;
            this.lightBeam.setTargetBeamProgress(0.1f);
            this.world().spawnEntity(this.lightBeam);
        }

        //Start first thread if not already started
        if (!hasStartedFirstThread)
        {
            hasStartedFirstThread = true;
            this.lightBeam.setTargetBeamProgress(0.2f);
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
                    final IBlockState state = world.getBlockState(blockPos); //TODO filter what can turn into a flying block

                    //Remove block
                    MinecraftForge.EVENT_BUS.post(new BlastBlockModifyEvent(world, blockPos, () -> {
                        EntityFlyingBlock entity = destroyBlock(world, blockPos, state);
                        if(entity != null) {
                            flyingBlocks.add(entity);
                        }
                    }));
                }

                blocksToRemove.clear();
            }

            //Delay second thread start
            if (secondThreadTimer-- <= 0)
            {
                this.lightBeam.setTargetBeamProgress(0.8f);
                hasStartedSecondThread = true;
                WorkerThreadManager.INSTANCE.addWork(getSecondThread());
            }
        }

        if (hasCompetedSecondThread)
        {
            this.lightBeam.setTargetBeamProgress(1f);
            if (!hasEnabledGravityForFlyingBlocks)
            {
                hasEnabledGravityForFlyingBlocks = true;
                flyingBlocks.forEach(entity -> entity.gravity = 0.5f);
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
        return new ThreadWorkBlast((steps, edits) -> collectFlyingBlocks(edits), edits ->
        {
            blocksToRemove.addAll(edits);
            hasGeneratedFlyingBlocks = false;
            hasCompletedFirstThread = true;
        });
    }

    protected IThreadWork getSecondThread()
    {
        return new ThreadWorkBlast((steps, edits) -> collectBlocksToMutate(edits), edits ->
        {
            blocksToRemove.addAll(edits);
            hasPlacedBlocks = false;
            hasCompetedSecondThread = true;
        });
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
                blockPos.setPos(location.x() + x, location.y() + y, location.z() + z);

                //Get block
                final IBlockState state = world.getBlockState(blockPos);
                final Block block = state.getBlock();

                //Validate TODO rework to not access blockstates, instead just collect positions
                if (!block.isAir(state, world, blockPos) && state.getBlockHardness(world, blockPos) >= 0)
                {
                    edits.accept(blockPos.toImmutable());
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
