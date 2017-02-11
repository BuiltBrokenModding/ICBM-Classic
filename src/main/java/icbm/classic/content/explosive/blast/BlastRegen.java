package icbm.classic.content.explosive.blast;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.ChunkProviderServer;

public class BlastRegen extends Blast
{
    public BlastRegen(World world, Entity entity, double x, double y, double z, float size)
    {
        super(world, entity, x, y, z, size);
    }

    @Override
    public void doExplode()
    {
        if (!world().isRemote)
        {
            try
            {
                Chunk oldChunk = world().getChunkFromBlockCoords(position.xi(), position.zi());

                if (world() instanceof WorldServer)
                {
                    WorldServer worldServer = (WorldServer) world();
                    ChunkProviderServer chunkProviderServer = worldServer.theChunkProviderServer;
                    IChunkProvider chunkProviderGenerate = chunkProviderServer.currentChunkProvider;
                    Chunk newChunk = chunkProviderGenerate.provideChunk(oldChunk.xPosition, oldChunk.zPosition);

                    for (int x = 0; x < 16; x++)
                    {
                        for (int z = 0; z < 16; z++)
                        {
                            for (int y = 0; y < world().getHeight(); y++)
                            {
                                Block blockID = newChunk.getBlock(x, y, z);
                                int metadata = newChunk.getBlockMetadata(x, y, z);

                                worldServer.setBlock(x + oldChunk.xPosition * 16, y, z + oldChunk.zPosition * 16, blockID, metadata, 2);
                            }
                        }
                    }

                    oldChunk.isTerrainPopulated = false;
                    chunkProviderGenerate.populate(chunkProviderGenerate, oldChunk.xPosition, oldChunk.zPosition);
                }
            }
            catch (Exception e)
            {
                System.out.println("ICBM Rejuvenation Failed!");
                e.printStackTrace();
            }
        }
    }

    @Override
    public long getEnergy()
    {
        return 0;
    }
}
