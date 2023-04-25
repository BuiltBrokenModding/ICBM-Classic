package icbm.classic.api.reg.obj;

import icbm.classic.ICBMClassic;
import icbm.classic.api.missiles.cause.IMissileSource;
import icbm.classic.api.missiles.parts.IBuildableObject;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;

import java.util.Collection;
import java.util.function.Supplier;

public interface IBuilderRegistry<Part extends IBuildableObject> {

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

    default NBTTagList save(Collection<Part> parts) {
        final NBTTagList list = new NBTTagList();
        for(Part part : parts) {
            final NBTTagCompound save = save(part);
            if(save != null) {
                list.appendTag(save);
            }
        }
        return list;
    }

    default NBTTagCompound save(Part part) {
        if(part == null) {
            ICBMClassic.logger().warn("Failed to save part due to null value", new RuntimeException());
            return null;
        }
        else if(part.getRegistryName() == null) {
            ICBMClassic.logger().warn("Failed to save part due to missing registry name: " + part, new RuntimeException());
            return null;
        }

        final NBTTagCompound save = new NBTTagCompound();
        save.setString("id", part.getRegistryName().toString());

        // Data is optional, only id is required as some objects are constants and need no save info
        final NBTTagCompound additionalData = part.serializeNBT();
        if (additionalData != null && !additionalData.hasNoTags()) {
            save.setTag("data", additionalData);
        }

        return save;
    }

    default <C extends Collection<Part>> C load(NBTTagList save, C list) {
        for(int i = 0; i < save.tagCount(); i++) {
            final Part part = load((NBTTagCompound) save.get(i));
            if(part != null) {
                list.add(part);
            }
        }
        return list;
    }

    default Part load(NBTTagCompound save) {
        if(save != null && !save.hasNoTags() && save.hasKey("id")) {
            final ResourceLocation id = new ResourceLocation(save.getString("id"));
            final Part part = build(id);
            if(part != null && save.hasKey("data")) {
                final NBTTagCompound additionalData = save.getCompoundTag("data");
                part.deserializeNBT(additionalData);
            }
            return part;
        }
        return null;
    }
}
