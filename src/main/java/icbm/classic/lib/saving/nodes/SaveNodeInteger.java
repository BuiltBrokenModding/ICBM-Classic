package icbm.classic.lib.saving.nodes;

import icbm.classic.lib.saving.NbtSaveNode;
import net.minecraft.nbt.IntTag;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class SaveNodeInteger<E> extends NbtSaveNode<E, IntTag>
{
    public SaveNodeInteger(final String name, Function<E, Integer> save, BiConsumer<E, Integer> load)
    {
        super(name,
            (obj) -> Optional.ofNullable(save.apply(obj)).map(IntTag::valueOf).orElse(null),
            (obj, data) -> load.accept(obj, data.getAsInt())
        );
    }
}
