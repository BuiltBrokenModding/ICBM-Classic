package icbm.classic.content.missile;

import icbm.classic.ICBMClassic;
import icbm.classic.api.missiles.parts.IBuildableObject;
import icbm.classic.api.reg.obj.IBuilderRegistry;
import lombok.RequiredArgsConstructor;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.FMLCommonHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

@RequiredArgsConstructor
public class MissilePartRegistry<Part extends IBuildableObject> implements IBuilderRegistry<Part>
{
    private final Map<ResourceLocation, Supplier<Part>> builders = new HashMap();
    private boolean locked = false;

    private final String name;

    @Override
    public void register(ResourceLocation name, Supplier<Part> builder) {
        if (locked) {
            throw new RuntimeException(name + ": mod '" + FMLCommonHandler.instance().getModName() + "' attempted to do a late registry");
        }
        if (builders.containsKey(name)) {
            throw new RuntimeException(name + ": mod '" + FMLCommonHandler.instance().getModName() + "' attempted to override '" + name + "'. " +
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
    public void overrideRegistry(ResourceLocation name, Supplier<Part> builder) {
        if (locked) {
            throw new RuntimeException(name + ":mod '" + FMLCommonHandler.instance().getModName() + "' attempted to do a late registry");
        }
        ICBMClassic.logger().info(name + ":'" + name + "' is being overridden by " + FMLCommonHandler.instance().getModName());
        builders.put(name, builder);
    }

    @Override
    public Part build(ResourceLocation name) {
        return Optional.ofNullable(builders.get(name)).map(Supplier::get).orElse(null);
    }

    public void lock() {
        this.locked = true;
    }
}
