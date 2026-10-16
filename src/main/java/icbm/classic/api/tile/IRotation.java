package icbm.classic.api.tile;


import net.minecraft.core.Direction;

/** Used by blocks that have a placement direction in the world
 *
 * Created by Robin on 12/9/2014.
 */
@Deprecated //Highly likely this will be replaced or removed
public interface IRotation
{
    /** Gets the facing direction of the BlockEntity
     * @return  Front of the tile */
    Direction getDirection();
}
// PORTED
