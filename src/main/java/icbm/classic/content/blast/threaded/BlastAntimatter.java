package icbm.classic.content.blast.threaded;

import icbm.classic.ICBMClassic;
import icbm.classic.client.ICBMSounds;
import icbm.classic.config.ConfigDebug;
import icbm.classic.config.blast.ConfigBlast;
import icbm.classic.config.blast.types.ConfigAntimatter;
import icbm.classic.content.blast.BlastHelpers;
import icbm.classic.content.blast.redmatter.EntityRedmatter;
import icbm.classic.lib.transform.BlockEditHandler;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import icbm.classic.content.blast.helpers.BlastBlockHelpers;
import java.util.concurrent.ThreadLocalRandom;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class BlastAntimatter extends BlastThreaded
{
    private final BlockState replaceState = Blocks.AIR.defaultBlockState();

    @Override
    public boolean setupBlast()
    {
        super.setupBlast();
        ICBMSounds.ANTIMATTER.play(world, this.location.x(), this.location.y(), this.location.z(), 7F, (float) (this.world().random.nextFloat() * 0.1 + 0.9F), true);
        return this.doDamageEntities(this.getBlastRadius() * 2, ConfigBlast.antimatter.damage); //TODO config for radius
    }

    @Override
    public void destroyBlock(BlockPos blockPos)
    {
        final BlockState blockState = world.getBlockState(blockPos);
        if (!blockState.isAir())
        {
            if (blockState.getDestroySpeed(world, blockPos) >= 0 || ConfigBlast.antimatter.damageUnbreakable)
            {
                world.setBlock(blockPos, replaceState, ConfigBlast.BLAST_DO_BLOCKUPDATES ? 3 : 2);
            }
        }
    }

    @Override
    public boolean doRun(int loops, Consumer<BlockPos> edits)
    {
        long time = System.currentTimeMillis();
        BlastHelpers.forEachPosInRadius(this.getBlastRadius(), (x, y, z) -> {
            if (shouldEditPos(x, y, z))
            {
                edits.accept(new BlockPos(xi() + x, yi() + y, zi() + z));
            }
        });
        time = System.currentTimeMillis() - time;
        if(ConfigDebug.DEBUG_EXPLOSIVES) {
            ICBMClassic.logger().info("Antimatter: thread calculations ran in " + time + "ms");
        }
        return false;
    }

    @Override
    protected void onWorkerThreadComplete(List<BlockPos> edits)
    {
        //TODO have threads take a copy of chunks for use before running
        //      OR replace thread system with an entity that ticks over time to collect data
        if (world instanceof ServerLevel)
        {
            long time = System.currentTimeMillis();

            //Sort distance
            edits.sort(buildSorter());

            time = System.currentTimeMillis() - time;
            if(ConfigDebug.DEBUG_EXPLOSIVES) {
                ICBMClassic.logger().info("Antimatter: sorting blocks ran in " + time + "ms");
            }

            time = System.currentTimeMillis();

            //Pull out fluids and falling blocks to prevent lag issues
            // PORT: worker thread, reads the chunks captured on the server thread
            final List<BlockPos> removeFirst = edits.stream()
                    .filter(this::isLoadedThreaded)
                    .filter(this::isFluid)
                    .collect(Collectors.toList());

            time = System.currentTimeMillis() - time;
            if(ConfigDebug.DEBUG_EXPLOSIVES) {
                ICBMClassic.logger().info("Antimatter: filtering first edits ran in " + time + "ms");
            }

            //Schedule edits to run in the world
            scheduleOnServerThread(() -> scheduledTask(removeFirst, edits));
        }
    }

    private void scheduledTask(List<BlockPos> removeFirst, List<BlockPos> edits) {

        //Worker is done reading
        releaseChunkSnapshot();

        long time = System.currentTimeMillis();

        //Remove any blocks that could cause issues when queued
        removeFirst.forEach(blockPos -> world.setBlock(blockPos, replaceState, 2));

        time = System.currentTimeMillis() - time;
        if(ConfigDebug.DEBUG_EXPLOSIVES) {
            ICBMClassic.logger().info("Antimatter: remove#first blocks ran in " + time + "ms,  edits:" + removeFirst.size());
        }

        time = System.currentTimeMillis();

        //Queue edits, even the ones from the previous
        BlockEditHandler.queue(world, edits, this::destroyBlock);

        time = System.currentTimeMillis() - time;
        if(ConfigDebug.DEBUG_EXPLOSIVES) {
            ICBMClassic.logger().info("Antimatter: remove#second ran in " + time + "ms,  edits:" + edits.size());
        }

        //Notify blast we have entered world again
        onPostThreadJoinWorld();
    }

    /** Called from the worker thread */
    protected boolean isFluid(BlockPos blockPos)
    {
        BlockState state = getBlockStateThreaded(blockPos);
        return BlastBlockHelpers.isWaterMaterial(state) || state.getBlock() instanceof FallingBlock;
    }

    protected boolean shouldEditPos(int x, int y, int z)
    {
        final double distSQ = x * x + y * y + z * z;
        final double blastSQ = this.getBlastRadius() * this.getBlastRadius();

        final int featherEdge = (int) Math.floor(blastSQ * 0.05f);
        final int delta = (int) Math.floor(blastSQ - distSQ);

        if (delta < featherEdge)
        {
            final double p2 = 1 - (delta / (double) featherEdge);
            return ThreadLocalRandom.current().nextFloat() /* PORT: worker thread, 1.21 level random throws when used from several threads */ > p2;
        }
        return true;
    }

    @Override
    public void onBlastCompleted()
    {
        super.onBlastCompleted();
        this.doDamageEntities(this.getBlastRadius() * 2, ConfigBlast.antimatter.damage); //TODO config for radius
    }

    @Override
    protected boolean doDamageEntities(float radius, float power, boolean destroyItem) {

        if (!ConfigBlast.antimatter.damageOnRedmatterKill)
        {
            final List<Entity> allEntities = getEntities(radius * 2); //TODO config for radius
            final long killed = allEntities.stream().filter(e -> e instanceof EntityRedmatter).peek(this::onDamageEntity).count();
            if(killed > 0) {
                return false;
            }
            return this.doDamageEntities(allEntities, radius, power, destroyItem);
        }
        return super.doDamageEntities(radius, power, destroyItem);
    }

    @Override
    protected boolean onDamageEntity(Entity entity)
    {
        if (entity instanceof EntityRedmatter)
        {
            //TODO implement a proper event instead of BlastCancel
            entity.discard();
            return true;
        }

        return false;
    }
}
// PORTED
