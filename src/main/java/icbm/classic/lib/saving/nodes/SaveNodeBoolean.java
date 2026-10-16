package icbm.classic.lib.saving.nodes;

import icbm.classic.lib.saving.NbtSaveNode;
import net.minecraft.nbt.ByteTag;

import java.util.function.BiConsumer;
import java.util.function.Function;

public class SaveNodeBoolean<E> extends NbtSaveNode<E, ByteTag>
{
    public SaveNodeBoolean(final String name, Function<E, Boolean> save, BiConsumer<E, Boolean> load)
    {
        super(name,
            (obj) -> save(save.apply(obj)),
            (obj, data) -> load.accept(obj, load(data))
        );
    }

    public static ByteTag save(Boolean b) {
        return ByteTag.valueOf((byte) (b ? 1 : 0));
    }

    public static boolean load(ByteTag tag) {
        return tag.getAsByte() == 1;
    }
}
