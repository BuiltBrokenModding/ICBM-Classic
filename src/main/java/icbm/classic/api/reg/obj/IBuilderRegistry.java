package icbm.classic.api.reg.obj;

import icbm.classic.ICBMClassic;
import icbm.classic.lib.RegistryHelper;
import net.minecraft.nbt.CollectionTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.util.INBTSerializable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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
    void register(@Nonnull ResourceLocation name, @Nonnull Supplier<Part> builder);

    /**
     * Builds/Get the object. Often for data sources this is
     * a global static version that is never rebuilt.
     *
     * @param name matching registry
     * @return instance or null if not registered
     */
    @Nullable
    Part getOrBuild(@Nonnull ResourceLocation name);

    /**
     * Unique name for this registry. Mostly used for translations
     *
     * @return unique builder key
     */
    @Nonnull
    String getUniqueName();

    default ListTag save(@Nonnull Collection<Part> parts) {
        final ListTag list = new ListTag();
        for(Part part : parts) {
            final CompoundTag save = save(part);
            if(save != null) {
                list.add(save);
            }
        }
        return list;
    }

    default CompoundTag save(@Nonnull Part part) {
        if(part == null) {
            ICBMClassic.logger().warn("Failed to save part due to null value", new RuntimeException());
            return null;
        }
        else if(part.getRegistryKey() == null) {
            ICBMClassic.logger().warn("Failed to save part due to missing registry name: " + part, new RuntimeException());
            return null;
        }

        final CompoundTag save = new CompoundTag();
        save.putString("id", part.getRegistryKey().toString());

        if(part instanceof INBTSerializable) {
            // Data is optional, only id is required as some objects are constants and need no save info
            final Tag additionalData = ((INBTSerializable<Tag>)part).serializeNBT(RegistryHelper.access());
            if (additionalData != null && (hasData(additionalData) || additionalData instanceof NumericTag)) {
                save.put("data", additionalData);
            }
        }

        return save;
    }

    default <C extends Collection<Part>> C load(@Nonnull ListTag save, @Nonnull C list) {
        for(int i = 0; i < save.size(); i++) {
            final Part part = load((CompoundTag) save.get(i));
            if(part != null) {
                list.add(part);
            }
        }
        return list;
    }

    default Part load(@Nullable CompoundTag save) {
        if(save != null && !save.isEmpty() && save.contains("id")) {
            final ResourceLocation id = ResourceLocation.parse(save.getString("id"));
            final Part part = getOrBuild(id);
            if(part instanceof INBTSerializable && save.contains("data")) {
                final Tag additionalData = save.get("data");
                ((INBTSerializable<Tag>)part).deserializeNBT(RegistryHelper.access(), additionalData);
            }
            return part;
        }
        return null;
    }

    boolean isLocked();

    /**
     * Replacement for 1.12 {@code !NBTBase#hasNoTags()}, which 1.21 {@link Tag} no longer has
     *
     * @param tag to check
     * @return true if the tag contains data
     */
    static boolean hasData(Tag tag) {
        if (tag instanceof CompoundTag compound) {
            return !compound.isEmpty();
        }
        else if (tag instanceof CollectionTag<?> collection) {
            return !collection.isEmpty();
        }
        else if (tag instanceof StringTag string) {
            return !string.getAsString().isEmpty();
        }
        return true;
    }
}
// PORTED
