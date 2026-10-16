package icbm.classic.api.data;

import net.minecraft.world.level.Level;

/**
 *
 * Created by Dark(DarkGuardsman, Robin) on 1/7/19.
 */
@FunctionalInterface
public interface WorldTickFunction
{
    void onTick(Level world, double x, double y, double z, int tick);
}
// PORTED
