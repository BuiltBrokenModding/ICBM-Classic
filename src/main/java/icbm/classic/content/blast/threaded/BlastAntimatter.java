package icbm.classic.content.blast.threaded;

import icbm.classic.ICBMClassic;
import icbm.classic.client.ICBMSounds;
import icbm.classic.config.ConfigDebug;
import icbm.classic.config.blast.ConfigBlast;
import icbm.classic.content.blast.BlastHelpers;
import icbm.classic.content.blast.redmatter.EntityRedmatter;
import icbm.classic.lib.transform.BlockEditHandler;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class BlastAntimatter extends BlastThreaded
{
    private final IBlockState replaceState = Blocks.AIR.getDefaultState();

    @Override
    public boolean setupBlast()
    {
        super.setupBlast();
        ICBMSounds.ANTIMATTER.play(world, this.location.x(), this.location.y(), this.location.z(), 7F, (float) (this.world().rand.nextFloat() * 0.1 + 0.9F), true);
        return this.doDamageEntities(this.getBlastRadius() * 2, Integer.MAX_VALUE);
    }

    @Override
    public void destroyBlock(BlockPos blockPos)
    {
        final IBlockState blockState = world.getBlockState(blockPos);
        if (!blockState.getBlock().isAir(blockState, world, blockPos))
        {
            if (blockState.getBlockHardness(world, blockPos) >= 0 || ConfigBlast.ANTIMATTER_DESTROY_UNBREAKABLE_BLOCKS)
            {
                world.setBlockState(blockPos, replaceState, ConfigBlast.BLAST_DO_BLOCKUPDATES ? 3 : 2);
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
        if (world instanceof WorldServer)
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
            final List<BlockPos> removeFirst = edits.stream()
                    .filter(blockPos -> world.isBlockLoaded(blockPos))
                    .filter(this::isFluid)
                    .collect(Collectors.toList());

            time = System.currentTimeMillis() - time;
            if(ConfigDebug.DEBUG_EXPLOSIVES) {
                ICBMClassic.logger().info("Antimatter: filtering first edits ran in " + time + "ms");
            }

            //Schedule edits to run in the world
            ((WorldServer) world).addScheduledTask(() -> scheduledTask(removeFirst, edits));
        }
    }

    private void scheduledTask(List<BlockPos> removeFirst, List<BlockPos> edits) {

        long time = System.currentTimeMillis();

        //Remove any blocks that could cause issues when queued
        removeFirst.forEach(blockPos -> world.setBlockState(blockPos, replaceState, 2));

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

    protected boolean isFluid(BlockPos blockPos)
    {
        IBlockState state = world.getBlockState(blockPos);
        return state.getMaterial() == Material.WATER || state.getBlock() instanceof BlockFalling;
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
            return world().rand.nextFloat() > p2;
        }
        return true;
    }

    @Override
    public void onBlastCompleted()
    {
        super.onBlastCompleted();
        this.doDamageEntities(this.getBlastRadius() * 2, Integer.MAX_VALUE);
    }

    @Override
    protected boolean onDamageEntity(Entity entity)
    {
        if (entity instanceof EntityRedmatter)
        {
            //TODO implement a proper event instead of BlastCancel
            entity.setDead();
            return true;
        }

        return false;
    }
}
