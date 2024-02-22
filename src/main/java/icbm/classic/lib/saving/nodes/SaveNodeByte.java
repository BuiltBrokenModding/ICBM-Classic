package icbm.classic.lib.saving.nodes;

import icbm.classic.lib.saving.NbtSaveNode;
import net.minecraft.nbt.NBTTagByte;
import net.minecraft.nbt.NBTTagInt;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class SaveNodeByte<E> extends NbtSaveNode<E, NBTTagByte>
{
    public SaveNodeByte(final String name, Function<E, Byte> save, BiConsumer<E, Byte> load)
    {
        super(name,
            (obj) -> Optional.ofNullable(save.apply(obj)).map(NBTTagByte::new).orElse(null),
            (obj, data) -> load.accept(obj, data.getByte())
        );
    }
}
