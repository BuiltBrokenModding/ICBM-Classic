package icbm.classic.api.caps;

import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

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
    void setPosition(@Nullable Vec3 position);

    /**
     * Sets the dimension
     *
     * @param world to set, can be set to null to clear
     */
    default void setWorld(@Nullable Level world) {
        setWorld(world != null ? world.dimension() : null);
    }

    /**
     * Sets the world
     *
     * @param dimension to set, can be set to null to clear
     */
    void setWorld(@Nullable ResourceKey<Level> dimension);

    /**
     * Gets the position component of the GPS data
     *
     * @return position
     */
    @Nullable
    Vec3 getPosition();

    /**
     * Gets the world instance, if client side use {@link #getWorldId()}
     *
     * @return world, server side only
     */
    @Nullable
    default Level getWorld() {
        final ResourceKey<Level> id = getWorldId();
        if(id != null) {
            final MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
            if(server != null) {
                return server.getLevel(id);
            }
        }
        return null;
    }

    /**
     * Gets the stored world id
     *
     * @return id
     */
    @Nullable
    ResourceKey<Level> getWorldId();
}
// PORTED
