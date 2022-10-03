package icbm.classic.content.missile.targeting.reg;

import icbm.classic.ICBMClassic;
import icbm.classic.api.missiles.IMissileTarget;
import icbm.classic.api.reg.obj.IMissileTargetReg;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.FMLCommonHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

public class MissileTargetRegistry implements IMissileTargetReg {

    private final Map<ResourceLocation, Supplier<IMissileTarget>> builders = new HashMap();
    private boolean locked = false;

    @Override
    public void register(ResourceLocation name, Supplier<IMissileTarget> builder) {
        if (locked) {
            throw new RuntimeException("MissileTargetRegistry: mod '" + FMLCommonHandler.instance().getModName() + "' attempted to do a late registry");
        }
        if (builders.containsKey(name)) {
            throw new RuntimeException("MissileTargetRegistry: mod '" + FMLCommonHandler.instance().getModName() + "' attempted to override '" + name + "'. " +
                    "This method does not allow replacing existing registries. See implementing class for override call.");
        }
        builders.put(name, builder);
    }

    /**
     * Use this to safely override another mod's content. Make sure to do a dependency on the mod to ensure
     * your mod loads after. Do not wait for the events to complete as the registry locks and will throw errors.
     *
     * @param name of the content to override
     * @param builder to use for save/load
     */
    public void overrideRegistry(ResourceLocation name, Supplier<IMissileTarget> builder) {
        if (locked) {
            throw new RuntimeException("MissileTargetRegistry: mod '" + FMLCommonHandler.instance().getModName() + "' attempted to do a late registry");
        }
        ICBMClassic.logger().info("MissileTargetRegistry: '" + name + "' is being overridden by " + FMLCommonHandler.instance().getModName());
        builders.put(name, builder);
    }

    @Override
    public IMissileTarget build(ResourceLocation name) {
        return Optional.ofNullable(builders.get(name)).map(Supplier::get).orElse(null);
    }

    public void lock() {
        this.locked = true;
    }
}
