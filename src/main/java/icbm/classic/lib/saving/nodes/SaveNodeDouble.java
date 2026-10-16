package icbm.classic.lib.saving.nodes;

import icbm.classic.lib.saving.NbtSaveNode;
import net.minecraft.nbt.DoubleTag;

import java.util.function.BiConsumer;
import java.util.function.Function;

public class SaveNodeDouble<E> extends NbtSaveNode<E, DoubleTag>
{
    public SaveNodeDouble(final String name, Function<E, Double> save, BiConsumer<E, Double> load)
    {
        super(name,
            (obj) -> DoubleTag.valueOf(save.apply(obj)),
            (obj, data) -> load.accept(obj, data.getAsDouble())
        );
    }
}
