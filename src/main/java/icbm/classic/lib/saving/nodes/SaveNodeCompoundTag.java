package icbm.classic.lib.saving.nodes;

import icbm.classic.lib.saving.NbtSaveNode;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import java.util.function.BiConsumer;
import java.util.function.Function;

public class SaveNodeCompoundTag<E> extends NbtSaveNode<E, NBTTagCompound>
{
    public SaveNodeCompoundTag(final String name, Function<E, NBTTagCompound> getter, BiConsumer<E, NBTTagCompound> setter) {
        super(name, getter, setter);
    }
}
