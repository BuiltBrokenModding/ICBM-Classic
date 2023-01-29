package icbm.classic.api.reg.obj;

import icbm.classic.api.missiles.IMissileTarget;
import net.minecraft.util.ResourceLocation;

import java.util.function.Supplier;

/**
 * Used to rebuild target data from saves
 */
public interface IMissileTargetReg {

    /**
     * Registers a new factory for loading target data from save file
     *
     * @param name to register with
     * @param builder to create new instances
     *
     * @throws RuntimeException if registry is locked or name is already used
     */
    void register(ResourceLocation name, Supplier<IMissileTarget> builder);

    /**
     * Builds a new target data instance
     *
     * @param name matching registry
     * @return new instance or null if not registered
     */
    IMissileTarget build(ResourceLocation name);
}
