package icbm.classic.api.reg.obj;

import icbm.classic.ICBMClassic;
import icbm.classic.api.missiles.parts.IBuildableObject;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;

import java.util.Collection;
import java.util.function.Supplier;

public interface IBuilderRegistry<Part extends IBuildableObject> {

    /**
     * Registers a new factory for loading the part
     *
     * @param name    to register with
     * @param builder to create new instances
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

    default ListTag save(Collection<Part> parts) {
        final ListTag list = new ListTag();
        for (Part part : parts) {
            final CompoundTag save = save(part);
            if (save != null) {
                list.add(save);
            }
        }
        return list;
    }

    default CompoundTag save(Part part) {
        if (part == null) {
            ICBMClassic.logger().warn("Failed to save part due to null value", new RuntimeException());
            return null;
        } else if (part.getRegistryName() == null) {
            ICBMClassic.logger().warn("Failed to save part due to missing registry name: " + part, new RuntimeException());
            return null;
        }

        final CompoundTag save = new CompoundTag();
        save.putString("id", part.getRegistryName().toString());

        // Data is optional, only id is required as some objects are constants and need no save info
        final CompoundTag additionalData = part.serializeNBT();
        if (additionalData != null && !additionalData.isEmpty()) {
            save.put("data", additionalData);
        }

        return save;
    }

    default <C extends Collection<Part>> C load(ListTag save, C list) {
        for (int i = 0; i < save.size(); i++) {
            final Part part = load((CompoundTag) save.get(i));
            if (part != null) {
                list.add(part);
            }
        }
        return list;
    }

    default Part load(CompoundTag save) {
        if (save != null && !save.isEmpty() && save.contains("id")) {
            final ResourceLocation id = new ResourceLocation(save.getString("id"));
            final Part part = build(id);
            if (part != null && save.contains("data")) {
                final CompoundTag additionalData = save.getCompound("data");
                part.deserializeNBT(additionalData);
            }
            return part;
        }
        return null;
    }
}
