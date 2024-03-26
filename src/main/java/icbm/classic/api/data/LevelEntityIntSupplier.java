package icbm.classic.api.data;

import net.minecraft.world.entity.Entity;

/**
 * Created by Dark(DarkGuardsman, Robert) on 1/7/19.
 */
@FunctionalInterface
public interface LevelEntityIntSupplier {
    /**
     * Calculates or retrieves an int based on the entity
     *
     * @param entity
     * @return
     */
    int get(Entity entity);
}
