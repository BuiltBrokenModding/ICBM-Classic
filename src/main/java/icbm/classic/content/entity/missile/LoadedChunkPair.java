package icbm.classic.content.entity.missile;

import net.minecraft.util.math.ChunkPos;

/**
 * Object representing a chunk that is queued to be unloaded later
 *
 * Created by GHXX on 8/5/2018.
 */
public class LoadedChunkPair
{
    public ChunkPos chunkPos; //Location of the chunk
    public int timeLeft;      //Time left until the unload should happen

    //Default constructor
    public LoadedChunkPair(ChunkPos cp, int unloadChunkCooldown)
    {
        this.chunkPos = cp;
        this.timeLeft = unloadChunkCooldown;
    }
}
