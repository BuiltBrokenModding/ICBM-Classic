package icbm.classic.lib.saving.nodes;

import icbm.classic.lib.saving.NbtSaveNode;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.math.Vec3d;

import java.util.function.BiConsumer;
import java.util.function.Function;

public class SaveNodeItemStack<E> extends NbtSaveNode<E, NBTTagCompound>
{
    public SaveNodeItemStack(final String name, Function<E, ItemStack> getter, BiConsumer<E, ItemStack> setter) {
        super(name,
            (obj) -> {
                final ItemStack itemStack = getter.apply(obj);
                if (itemStack != null && !itemStack.isEmpty())
                {
                    return itemStack.writeToNBT(new NBTTagCompound());
                }
                return null;
            },
            (obj, data) -> {
                setter.accept(obj, new ItemStack(data));
            }
        );
    }
}
