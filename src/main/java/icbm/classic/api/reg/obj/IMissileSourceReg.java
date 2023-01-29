package icbm.classic.api.reg.obj;

import icbm.classic.api.missiles.IMissileSource;
import net.minecraft.util.ResourceLocation;

import java.util.function.Supplier;

public interface IMissileSourceReg
{
    /**
     * Registers a new factory for loading missile sources from save file
     *
     * @param name to register with
     * @param builder to create new instances
     *
     * @throws RuntimeException if registry is locked or name is already used
     */
    void register(ResourceLocation name, Supplier<IMissileSource> builder);

    /**
     * Builds a new missile source instance
     *
     * @param name matching registry
     * @return new instance or null if not registered
     */
    IMissileSource build(ResourceLocation name);
}
