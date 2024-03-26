package icbm.classic.api.data;

import net.minecraft.world.entity.Entity;

/**
 * Created by Dark(DarkGuardsman, Robert) on 1/7/19.
 */
@FunctionalInterface
public interface EntityTickFunction {
    void onTick(Entity entity, int tick);
}
