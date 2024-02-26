package icbm.classic.api.missiles.parts;

import icbm.classic.api.reg.obj.IBuilderRegistry;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.util.INBTSerializable;

import javax.annotation.Nonnull;

/**
 * Implemented by objects that can be recreated from save or packet data
 *
 * Most {@link IBuilderRegistry} supprt {@link INBTSerializable} for complex save/load. Without it
 * systems will only save the {@link #getRegistryKey()} for restoring the object. Which in many cases
 * may be enough to persist data.
 */
public interface IBuildableObject {

    /**
     * Name of the type of part. Used for save/load
     *
     * @return registry name
     */
    @Nonnull
    ResourceLocation getRegistryKey();

    /**
     * Registry used for this object.
     *
     * This will be used for a translations, save/load, and other tasks involving
     * referencing back to the registry. It shouldn't be used for type identification.
     * As content to the player might be spread over several registries while meaning the
     * same concept.
     *
     * @return registry used for this object
     */
    @Nonnull
    IBuilderRegistry getRegistry();

    /**
     * Gets the translation key for display in tooltips and
     * other user feedback locations.
     *
     * This is more meant to  act as a base translation key or prefix.
     * Think of it much like `tile.mod:entry` where `.name` or `.info` would
     * be added depending on context.
     *
     * @return key to use
     */
    @Nonnull
    default String getTranslationKey() {
        return String.format("%s.%s", this.getRegistry().getUniqueName(), this.getRegistryKey());
    }

    /**
     * Tooltip information to show in GUIs or on an item
     *
     * Calling systems often use {@link icbm.classic.lib.LanguageUtility#buildToolTipString(ITextComponent)}
     * to add extra features such as line splitting, indents, and colors
     *
     * @return text component for display
     */
    @Nonnull
    default ITextComponent getTooltip() {
        return new TextComponentTranslation(getTranslationKey());
    }
}
