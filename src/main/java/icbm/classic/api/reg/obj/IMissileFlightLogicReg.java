package icbm.classic.api.reg.obj;

import icbm.classic.api.missiles.IMissileFlightLogic;
import icbm.classic.api.missiles.IMissileTarget;
import net.minecraft.util.ResourceLocation;

import java.util.function.Supplier;

/**
 * Used to rebuild flight logic from saves
 */
public interface IMissileFlightLogicReg
{

    /**
     * Registers a new factory for loading flight logic from save file
     *
     * @param name to register with
     * @param builder to create new instances
     *
     * @throws RuntimeException if registry is locked or name is already used
     */
    void register(ResourceLocation name, Supplier<IMissileFlightLogic> builder);

    /**
     * Builds a new flight logic instance
     *
     * @param name matching registry
     * @return new instance or null if not registered
     */
    IMissileFlightLogic build(ResourceLocation name);
}
