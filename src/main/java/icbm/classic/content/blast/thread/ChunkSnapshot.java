package icbm.classic.content.blast.thread;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;

import javax.annotation.Nullable;

/**
 * Loaded chunks around a blast, collected on the server thread so worker threads can read block states.
 * <p>
 * PORT: 1.12 worker threads called World#getBlockState, which was a plain chunk map lookup. In 1.21
 * ServerChunkCache#getChunk hands every call made from another thread to the server thread and waits for it,
 * so a threaded blast doing millions of reads crawls on a busy server and blocks forever once the server stops.
 * Workers read the chunk sections directly through this class instead.
 * <p>
 * The chunks are referenced, not copied, so edits made after the capture are seen like they were in 1.12.
 * Chunks that were not loaded when captured count as not loaded, workers skip them like 1.12 skipped
 * positions failing World#isBlockLoaded.
 */
public final class ChunkSnapshot
{
    private final Long2ObjectMap<LevelChunk> chunks;
    private final int minBuildHeight;
    /** Exclusive */
    private final int maxBuildHeight;

    private ChunkSnapshot(Long2ObjectMap<LevelChunk> chunks, int minBuildHeight, int maxBuildHeight)
    {
        this.chunks = chunks;
        this.minBuildHeight = minBuildHeight;
        this.maxBuildHeight = maxBuildHeight;
    }

    /**
     * Collects the loaded chunks inside the square around the center. Must be called on the thread that owns the
     * level (server thread), ServerChunkCache#getChunkNow returns null on any other thread.
     *
     * @param level   - level to read, null gives an empty snapshot
     * @param centerX - block x of the center
     * @param centerZ - block z of the center
     * @param radius  - blocks to cover on each side of the center
     * @return snapshot, never null
     */
    public static ChunkSnapshot capture(@Nullable Level level, double centerX, double centerZ, int radius)
    {
        final Long2ObjectOpenHashMap<LevelChunk> chunks = new Long2ObjectOpenHashMap<>();
        if (level == null)
        {
            return new ChunkSnapshot(chunks, 0, 0);
        }

        final int blockX = Mth.floor(centerX);
        final int blockZ = Mth.floor(centerZ);
        final int minChunkX = SectionPos.blockToSectionCoord(blockX - radius);
        final int maxChunkX = SectionPos.blockToSectionCoord(blockX + radius);
        final int minChunkZ = SectionPos.blockToSectionCoord(blockZ - radius);
        final int maxChunkZ = SectionPos.blockToSectionCoord(blockZ + radius);

        for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++)
        {
            for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++)
            {
                // Only already loaded chunks, never loads or generates
                final LevelChunk chunk = level.getChunkSource().getChunkNow(chunkX, chunkZ);
                if (chunk != null)
                {
                    chunks.put(ChunkPos.asLong(chunkX, chunkZ), chunk);
                }
            }
        }
        return new ChunkSnapshot(chunks, level.getMinBuildHeight(), level.getMaxBuildHeight());
    }

    /**
     * Thread safe replacement for {@link Level#isLoaded(BlockPos)}
     *
     * @param pos - position
     * @return true if the position is inside the build height and its chunk was loaded when captured
     */
    public boolean isLoaded(BlockPos pos)
    {
        return pos.getY() >= minBuildHeight && pos.getY() < maxBuildHeight && getChunk(pos) != null;
    }

    /**
     * Thread safe replacement for {@link Level#getBlockState(BlockPos)}
     *
     * @param pos - position
     * @return block state, air if the chunk was not loaded, void air outside the build height
     */
    public BlockState getBlockState(BlockPos pos)
    {
        if (pos.getY() < minBuildHeight || pos.getY() >= maxBuildHeight)
        {
            return Blocks.VOID_AIR.defaultBlockState();
        }

        final LevelChunk chunk = getChunk(pos);
        if (chunk == null)
        {
            return Blocks.AIR.defaultBlockState();
        }

        try
        {
            final BlockState state = chunk.getBlockState(pos);
            return state != null ? state : Blocks.AIR.defaultBlockState();
        }
        catch (RuntimeException e)
        {
            // Palette was resized by the server thread while reading, treat as air like a failed read
            return Blocks.AIR.defaultBlockState();
        }
    }

    @Nullable
    private LevelChunk getChunk(BlockPos pos)
    {
        return chunks.get(ChunkPos.asLong(SectionPos.blockToSectionCoord(pos.getX()), SectionPos.blockToSectionCoord(pos.getZ())));
    }
}
// PORTED
