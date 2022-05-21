package icbm.classic.lib.saving.nodes;

import icbm.classic.lib.saving.NbtSaveNode;
import net.minecraft.nbt.NBTTagInt;

import java.util.function.BiConsumer;
import java.util.function.Function;

public class SaveNodeInteger<E> extends NbtSaveNode<E, NBTTagInt>
{
    public SaveNodeInteger(final String name, Function<E, Integer> save, BiConsumer<E, Integer> load)
    {
        super(name,
            (obj) -> new NBTTagInt(save.apply(obj)),
            (obj, data) -> load.accept(obj, data.getInt())
        );
    }
}
