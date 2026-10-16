package icbm.classic.lib;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

import java.util.function.Consumer;

/**
 * Replacement for 1.12 {@code ItemStack#getTagCompound()} style access. Data is stored in the vanilla
 * {@link DataComponents#CUSTOM_DATA} component so it saves, syncs and compares like stack NBT did.
 */
public final class StackNbt
{
    private StackNbt()
    {
    }

    /** @return copy of the stack's custom tag, empty if none. Mutating it does not change the stack. */
    public static CompoundTag get(ItemStack stack)
    {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
    }

    /** @return true if the stack has custom data with the given key */
    public static boolean has(ItemStack stack, String key)
    {
        final CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        return data != null && data.contains(key);
    }

    /** @return true if the stack has any custom data */
    public static boolean has(ItemStack stack)
    {
        final CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        return data != null && !data.isEmpty();
    }

    /** @return copy of the compound stored under key, empty if missing */
    public static CompoundTag getCompound(ItemStack stack, String key)
    {
        return get(stack).getCompound(key);
    }

    /** Replaces the stack's whole custom tag, removing the component when empty */
    public static void set(ItemStack stack, CompoundTag tag)
    {
        if (tag == null || tag.isEmpty())
        {
            stack.remove(DataComponents.CUSTOM_DATA);
        }
        else
        {
            CustomData.set(DataComponents.CUSTOM_DATA, stack, tag);
        }
    }

    /** Edits the stack's custom tag in place */
    public static void update(ItemStack stack, Consumer<CompoundTag> editor)
    {
        final CompoundTag tag = get(stack);
        editor.accept(tag);
        set(stack, tag);
    }

    /** Stores a compound under the given key, removes the key when the compound is null or empty */
    public static void putCompound(ItemStack stack, String key, CompoundTag value)
    {
        update(stack, tag -> {
            if (value == null || value.isEmpty())
            {
                tag.remove(key);
            }
            else
            {
                tag.put(key, value);
            }
        });
    }

    public static void remove(ItemStack stack, String key)
    {
        if (has(stack, key))
        {
            update(stack, tag -> tag.remove(key));
        }
    }
}
