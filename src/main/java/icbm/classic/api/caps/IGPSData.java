package icbm.classic.api.caps;

import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraftforge.common.DimensionManager;

import javax.annotation.Nullable;
import java.util.Optional;

/**
 * Capability used to access GPS data from an item
 */
public interface IGPSData {

    /**
     * Sets the stored location
     *
     * @param position to set, can be set to null to clear
     */
    void setPosition(@Nullable Vec3d position);

    /**
     * Sets the dimension
     *
     * @param world to set, can be set to null to clear
     */
    default void setWorld(World world) {
        setWorld(Optional.ofNullable(world).map(w -> w.provider).map(WorldProvider::getDimension).orElse(null));
    }

    /**
     * Sets the world
     *
     * @param dimension to set, can be set to null to clear
     */
    void setWorld(@Nullable Integer dimension);

    /**
     * Gets the position component of the GPS data
     *
     * @return position
     */
    @Nullable
    Vec3d getPosition();

    /**
     * Gets the world instance, if client side use {@link #getWorldId()}
     *
     * @return world
     */
    @Nullable
    default World getWorld() {
        final Integer id = getWorldId();
        if(id != null) {
            return DimensionManager.getWorld(id);
        }
        return null;
    }

    /**
     * Gets the stored world id
     *
     * @return id
     */
    @Nullable
    Integer getWorldId();
}
