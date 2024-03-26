package icbm.classic.lib.saving.nodes;

import icbm.classic.lib.saving.NbtSaveNode;
import net.minecraft.nbt.NBTTagByte;

import java.util.function.BiConsumer;
import java.util.function.Function;

public class SaveNodeBoolean<E> extends NbtSaveNode<E, NBTTagByte> {
    public SaveNodeBoolean(final String name, Function<E, Boolean> save, BiConsumer<E, Boolean> load) {
        super(name,
            (obj) -> new NBTTagByte((byte) (save.apply(obj) ? 1 : 0)),
            (obj, data) -> load.accept(obj, data.getByte() == 1)
        );
    }
}
