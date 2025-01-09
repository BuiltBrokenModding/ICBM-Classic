package icbm.classic.api.caps;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;

import javax.annotation.Nullable;

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

    default void setWorld(@Nullable World world) {
        setDimension(world != null ? world.getDimension().getType() : null);
    }

    default void setDimension(@Nullable DimensionType dimension) {
        setDimensionKey(dimension != null ? DimensionType.getKey(dimension) : null);
    }

    void setDimensionKey(@Nullable ResourceLocation dimension);

    /**
     * Gets the position component of the GPS data
     *
     * @return position
     */
    @Nullable
    Vec3d getPosition();

    /**
     * Gets the stored world id
     *
     * @return id
     */
    @Nullable
    ResourceLocation getDimensionKey();
}
