package icbm.classic.lib.saving.nodes;

import icbm.classic.lib.saving.NbtSaveNode;
import net.minecraft.nbt.CompoundTag;

import java.util.function.BiConsumer;
import java.util.function.Function;

public class SaveNodeCompoundTag<E> extends NbtSaveNode<E, CompoundTag>
{
    public SaveNodeCompoundTag(final String name, Function<E, CompoundTag> getter, BiConsumer<E, CompoundTag> setter) {
        super(name, getter, setter);
    }
}
