package icbm.classic.content.explosive.blast;

import icbm.classic.ICBMClassic;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraft.world.gen.IChunkGenerator;

public class BlastRegen extends Blast
{
    public BlastRegen(World world, Entity entity, double x, double y, double z, float size)
    {
        super(world, entity, x, y, z, size);
    }

    @Override
    public void doExplode()
    {
        if (!oldWorld().isRemote)
        {
            try
            {
                Chunk oldChunk = oldWorld().getChunkFromChunkCoords(position.xi() >> 4, position.zi() >> 4);

                if (oldWorld() instanceof WorldServer)
                {

                    IChunkProvider provider = oldWorld().getChunkProvider();
                    IChunkGenerator generator = ((ChunkProviderServer) provider).chunkGenerator;
                    Chunk newChunk = generator.generateChunk(oldChunk.x, oldChunk.z);

                    for (int x = 0; x < 16; x++)
                    {
                        for (int z = 0; z < 16; z++)
                        {
                            for (int y = 0; y < oldWorld().getHeight(); y++)
                            {
                                IBlockState state = newChunk.getBlockState(x, y, z);
                                world.setBlockState(new BlockPos(x + oldChunk.x * 16, y, z + oldChunk.z * 16), state, 3);
                            }
                        }
                    }

                    oldChunk.setTerrainPopulated(false);
                    generator.populate(oldChunk.x, oldChunk.z);
                    oldChunk.markDirty();
                    oldChunk.resetRelightChecks();
                }
            }
            catch (Exception e)
            {
                ICBMClassic.INSTANCE.logger().error("ICBM Rejuvenation Failed!", e);
            }
        }
    }

    @Override
    public long getEnergy()
    {
        return 0;
    }
}
