package icbm.classic.lib.saving.nodes;

import icbm.classic.lib.saving.NbtSaveNode;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.NbtUtils;

import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class SaveNodeUUID<E> extends NbtSaveNode<E, IntArrayTag>
{
    public SaveNodeUUID(final String name, Function<E, UUID> save, BiConsumer<E, UUID> load)
    {
        super(name,
            (obj) -> {
                final UUID blockState = save.apply(obj);
                if (blockState != null)
                {
                    return NbtUtils.createUUID(blockState);
                }
                return null;
            },
            (obj, data) -> {
                load.accept(obj, NbtUtils.loadUUID(data));
            }
        );
    }
}
