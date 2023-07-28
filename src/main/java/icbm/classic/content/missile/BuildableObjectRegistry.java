package icbm.classic.content.missile;

import icbm.classic.ICBMClassic;
import icbm.classic.api.missiles.parts.IBuildableObject;
import icbm.classic.api.reg.obj.IBuilderRegistry;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.FMLCommonHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

@RequiredArgsConstructor
public class BuildableObjectRegistry<Part extends IBuildableObject> implements IBuilderRegistry<Part>
{
    private final Map<ResourceLocation, Supplier<Part>> builders = new HashMap();
    @Getter
    private boolean isLocked = false;

    private final String name;

    @Override
    public void register(ResourceLocation key, Supplier<Part> builder) {
        if (isLocked) {
            throw new RuntimeException(this.name + ": mod '" + FMLCommonHandler.instance().getModName() + "' attempted to do a late registry");
        }
        if (builders.containsKey(key)) {
            throw new RuntimeException(this.name + ": mod '" + FMLCommonHandler.instance().getModName() + "' attempted to override '" + key + "'. " +
                    "This method does not allow replacing existing registries. See implementing class for override call.");
        }
        builders.put(key, builder);
    }

    /**
     * Use this to safely override another mod's content. Make sure to do a dependency on the mod to ensure
     * your mod loads after. Do not wait for the events to complete as the registry locks and will throw errors.
     *
     * @param key of the content to override
     * @param builder to use for save/load
     */
    public void overrideRegistry(ResourceLocation key, Supplier<Part> builder) {
        if (isLocked) {
            throw new RuntimeException(this.name + ":mod '" + FMLCommonHandler.instance().getModName() + "' attempted to do a late registry");
        }
        ICBMClassic.logger().info(this.name + ":'" + key + "' is being overridden by " + FMLCommonHandler.instance().getModName());
        builders.put(key, builder);
    }

    @Override
    public Part build(ResourceLocation name) {
        return Optional.ofNullable(builders.get(name)).map(Supplier::get).orElse(null);
    }

    public void lock() {
        this.isLocked = true;
    }
}
