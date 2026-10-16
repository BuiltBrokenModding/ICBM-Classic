package icbm.classic.api.data;

import net.minecraft.world.level.Level;

/**
 * Created by Dark(DarkGuardsman, Robin) on 1/7/19.
 */
@FunctionalInterface
public interface WorldPosIntSupplier
{
    /**
     * Calculates or retrieves an int based on the world and position
     *
     * @param world
     * @param x
     * @param y
     * @param z
     * @return
     */
    int get(Level world, double x, double y, double z);
}
// PORTED
