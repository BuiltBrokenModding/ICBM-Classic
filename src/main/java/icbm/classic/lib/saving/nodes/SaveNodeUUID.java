package icbm.classic.lib.saving.nodes;

import icbm.classic.lib.saving.NbtSaveNode;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NBTUtil;

import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class SaveNodeUUID<E> extends NbtSaveNode<E, CompoundTag> {
    public SaveNodeUUID(final String name, Function<E, UUID> save, BiConsumer<E, UUID> load) {
        super(name,
            (obj) -> {
                final UUID blockState = save.apply(obj);
                if (blockState != null) {
                    return NBTUtil.createUUIDTag(blockState);
                }
                return null;
            },
            (obj, data) -> {
                load.accept(obj, NBTUtil.getUUIDFromTag(data));
            }
        );
    }
}
