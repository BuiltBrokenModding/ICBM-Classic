package icbm.classic.lib.saving.nodes;

import icbm.classic.lib.saving.NbtSaveNode;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagFloat;

import java.util.function.BiConsumer;
import java.util.function.Function;

public class SaveNodeFloat<E> extends NbtSaveNode<E, NBTTagFloat>
{
    public SaveNodeFloat(final String name, Function<E, Float> save, BiConsumer<E, Float> load)
    {
        super(name,
            (obj) -> new NBTTagFloat(save.apply(obj)),
            (obj, data) -> load.accept(obj, data.getFloat())
        );
    }
}
