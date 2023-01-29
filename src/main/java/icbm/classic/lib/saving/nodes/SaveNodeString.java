package icbm.classic.lib.saving.nodes;

import icbm.classic.lib.saving.NbtSaveNode;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagString;

import java.util.function.BiConsumer;
import java.util.function.Function;

public class SaveNodeString<E> extends NbtSaveNode<E, NBTTagString>
{
    public SaveNodeString(final String name, Function<E, String> save, BiConsumer<E, String> load)
    {
        super(name,
            (obj) -> new NBTTagString(save.apply(obj)),
            (obj, data) -> load.accept(obj, data.getString())
        );
    }
}
