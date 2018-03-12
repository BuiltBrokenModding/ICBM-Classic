package icbm.classic.api.tile;

import icbm.classic.api.explosion.IBlast;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/** Applied to all blocks that has a custom reaction to EMPs. Blocks not TileEntities.
 *
 * @author Calclavia */
@Deprecated //Will be turned into a capability for TileEntity objects (and block state for Blocks?... maybe)
public interface IEMPBlock
{
    /** Called when this block gets attacked by EMP.
     *
     * @param world - The world object.
     * @param empExplosive - The explosion */
    void onEMP(World world, BlockPos pos, IBlast empExplosive);
}
