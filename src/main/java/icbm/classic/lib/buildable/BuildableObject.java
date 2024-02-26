package icbm.classic.lib.buildable;

import icbm.classic.api.missiles.parts.IBuildableObject;
import icbm.classic.api.reg.obj.IBuilderRegistry;
import icbm.classic.lib.saving.NbtSaveHandler;
import lombok.AccessLevel;
import lombok.Getter;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Template for creating new buildable object types
 * @param <T> this
 */
public abstract class BuildableObject<T extends IBuildableObject, B extends IBuilderRegistry> implements IBuildableObject {

    @Getter @Nonnull
    private final ResourceLocation registryKey;
    @Getter @Nonnull
    private final B registry;
    @Getter(value = AccessLevel.PROTECTED) @Nullable
    private final NbtSaveHandler<T> saveHandler;

    /**
     * Required constructor to feed in references.
     *
     * @param registryKey used to register this type with {@link #registry}, should be static value pass through
     * @param registry used to register the type, should be static value pass through
     * @param saveHandler to use for save/load handling, optional and should be static value pass through
     */
    public BuildableObject(@Nonnull ResourceLocation registryKey, @Nonnull B registry, @Nullable NbtSaveHandler<T> saveHandler) {
        this.registryKey = registryKey;
        this.registry = registry;
        this.saveHandler = saveHandler;
    }


    public NBTTagCompound serializeNBT() {
        return saveHandler != null ? saveHandler.save((T) this) : null;
    }

    public void deserializeNBT(NBTTagCompound nbt) {
        if(saveHandler != null) {
            saveHandler.load((T) this, nbt);
        }
    }
}
