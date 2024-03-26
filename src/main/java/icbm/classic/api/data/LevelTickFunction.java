package icbm.classic.api.data;

import net.minecraft.world.level.Level;

/**
 * Created by Dark(DarkGuardsman, Robert) on 1/7/19.
 */
@FunctionalInterface
public interface LevelTickFunction {
    void onTick(Level level, double x, double y, double z, int tick);
}
