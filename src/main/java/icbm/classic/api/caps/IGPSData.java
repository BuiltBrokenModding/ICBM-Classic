package icbm.classic.api.caps;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

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
    void setPosition(@Nullable Vec3 position);

    /**
     * Sets the dimension
     *
     * @param level to set, can be set to null to clear
     */
    default void setLevel(@Nullable Level level) {
        setLevel(Optional.ofNullable(level).map(Level::dimension).orElse(null));
    }

    /**
     * Sets the world
     *
     * @param dimension to set, can be set to null to clear
     */
    void setLevel(@Nullable ResourceKey<Level> dimension);

    /**
     * Gets the position component of the GPS data
     *
     * @return position
     */
    @Nullable
    Vec3 getPosition();

    /**
     * Gets the world instance, if client side use {@link #getLevelId()}
     *
     * @return world
     */
    @Nullable
    default Level getLevel() {
        final Integer id = getLevelId();
        if (id != null) {
            return DimensionManager.getLevel(id);
        }
        return null;
    }

    /**
     * Gets the stored world id
     *
     * @return id
     */
    @Nullable
    ResourceKey<Level> getLevelId();
}
