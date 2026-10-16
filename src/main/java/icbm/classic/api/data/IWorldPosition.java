package icbm.classic.api.data;

import com.builtbroken.jlib.data.vector.IPos3D;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;

/**
 * Useful interface to define that an object has a 3D location, and a defined world.
 *
 * @author DarkGuardsman
 */
@Deprecated
public interface IWorldPosition extends IPos3D
{
    Level world();

    default boolean isClient()
    {
        return hasWorld() && world().isClientSide();
    }

    default boolean isServer()
    {
        return hasWorld() && !world().isClientSide();
    }

    default boolean hasWorld()
    {
        return world() != null;
    }

    default BlockPos getPos()
    {
        return new BlockPos(xi(), yi(), zi());
    }

    default Vec3 getVec3d() {
        return new Vec3(x(), y(), z());
    }
}
// PORTED
