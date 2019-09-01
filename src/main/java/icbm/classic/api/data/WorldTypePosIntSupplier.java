package icbm.classic.api.data;

import icbm.classic.api.EnumExplosiveType;
import net.minecraft.world.World;

/**
 * Created by Dark(DarkGuardsman, Robert) on 1/7/19.
 */
@FunctionalInterface
public interface WorldTypePosIntSupplier
{
    /**
     * Calculates or retrieves an int based on the type and position
     *
     * @param world
     * @param x
     * @param y
     * @param z
     * @param tier
     * @return
     */
    int get(World world, EnumExplosiveType tier, double x, double y, double z);
}
