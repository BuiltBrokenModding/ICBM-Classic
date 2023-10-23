package icbm.classic.api.data;

import com.builtbroken.jlib.data.vector.IPos3D;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**
 * Useful interface to define that an object has a 3D location, and a defined world.
 *
 * @author DarkGuardsman
 */
@Deprecated
public interface IWorldPosition extends IPos3D
{
    World world();

    default boolean isClient()
    {
        return hasWorld() && world().isRemote;
    }

    default boolean isServer()
    {
        return hasWorld() && !world().isRemote;
    }

    default boolean hasWorld()
    {
        return world() != null;
    }

    default BlockPos getPos()
    {
        return new BlockPos(xi(), yi(), zi());
    }

    default Vec3d getVec3d() {
        return new Vec3d(x(), y(), z());
    }
}
