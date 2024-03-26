package icbm.classic.api.data;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Useful interface to define that an object has a 3D location, and a defined world.
 *
 * @author DarkGuardsman
 */
@Deprecated
public interface IWorldPosition {
    double x();

    double y();

    double z();

    Level level();

    default boolean isClient() {
        return hasLevel() && level().isClientSide();
    }

    default boolean isServer() {
        return hasLevel() && !level().isClientSide();
    }

    default boolean hasLevel() {
        return level() != null;
    }

    default BlockPos getBlockPos() {
        return BlockPos.containing(x(), y(), z());
    }

    default Vec3 getPos() {
        return new Vec3(x(), y(), z());
    }
}
