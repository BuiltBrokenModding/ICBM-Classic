package icbm.classic.content.blast;

import icbm.classic.ICBMClassic;
import icbm.classic.config.ConfigBlast;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraft.world.gen.IChunkGenerator;

public class BlastRegen extends Blast
{
    @Override
    public void doRunBlast() //TODO set up to scale
    {
        if (!world().isRemote)
        {
            try
            {
                Chunk oldChunk = world().getChunk(location.xi() >> 4, location.zi() >> 4);

                if (world() instanceof WorldServer)
                {

                    IChunkProvider provider = world().getChunkProvider();
                    IChunkGenerator generator = ((ChunkProviderServer) provider).chunkGenerator;
                    Chunk newChunk = generator.generateChunk(oldChunk.x, oldChunk.z);

                    for (int x = 0; x < 16; x++)
                    {
                        for (int z = 0; z < 16; z++)
                        {
                            for (int y = 0; y < world().getHeight(); y++)
                            {
                                IBlockState state = newChunk.getBlockState(x, y, z);
                                world.setBlockState(new BlockPos(x + oldChunk.x * 16, y, z + oldChunk.z * 16), state, 3);

                                world.markBlockRangeForRenderUpdate(new BlockPos(x + oldChunk.x * 16, y, z + oldChunk.z * 16),new BlockPos(x + oldChunk.x * 16, y, z + oldChunk.z * 16));
                                world.notifyBlockUpdate(new BlockPos(x + oldChunk.x * 16, y, z + oldChunk.z * 16),state,state,3);
                                world.scheduleBlockUpdate(new BlockPos(x + oldChunk.x * 16, y, z + oldChunk.z * 16),state.getBlock(),100,0);
                            }

                            if (ConfigBlast.REJUVENATION_REGEN_STRUCTURES)
                            {
                                generator.recreateStructures(oldChunk, x, z);
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
                ICBMClassic.logger().error("ICBM Rejuvenation Failed!", e);
            }
        }
    }
}
