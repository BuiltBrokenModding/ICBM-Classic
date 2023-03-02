package icbm.classic.api.reg.obj;

import icbm.classic.api.missiles.parts.IMissilePart;
import net.minecraft.util.ResourceLocation;

import java.util.function.Supplier;

public interface IMissilePartReg<Part extends IMissilePart> {

    /**
     * Registers a new factory for loading the part
     *
     * @param name to register with
     * @param builder to create new instances
     *
     * @throws RuntimeException if registry is locked or name is already used
     */
    void register(ResourceLocation name, Supplier<Part> builder);

    /**
     * Builds a new target data instance
     *
     * @param name matching registry
     * @return new instance or null if not registered
     */
    Part build(ResourceLocation name);
}
