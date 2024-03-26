package icbm.classic.api.data;

import net.minecraft.world.level.Level;

/**
 * Created by Dark(DarkGuardsman, Robert) on 1/7/19.
 */
@FunctionalInterface
public interface LevelPosIntSupplier {
    /**
     * Calculates or retrieves an int based on the level and position
     *
     * @param level
     * @param x
     * @param y
     * @param z
     * @return
     */
    int get(Level level, double x, double y, double z);
}
