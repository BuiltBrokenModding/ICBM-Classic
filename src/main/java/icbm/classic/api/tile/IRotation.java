package icbm.classic.api.tile;


import net.minecraft.util.EnumFacing;

/** Used by blocks that have a placement direction in the world
 *
 * Created by robert on 12/9/2014.
 */
@Deprecated //Highly likely this will be replaced or removed
public interface IRotation
{
    /** Gets the facing direction of the TileEntity
     * @return  Front of the tile */
    EnumFacing getDirection();
}
