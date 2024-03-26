package icbm.classic.lib.saving.nodes;

import icbm.classic.lib.saving.NbtSaveNode;
import net.minecraft.core.Direction;
import net.minecraft.nbt.NBTTagByte;

import java.util.function.BiConsumer;
import java.util.function.Function;

public class SaveNodeFacing<E> extends NbtSaveNode<E, NBTTagByte> //TODO convert to enum save/load (small enums can do short, large can do ints)
{
    public SaveNodeFacing(String name, Function<E, Direction> save, BiConsumer<E, Direction> load) {
        super(name,
            (obj) -> {
                final Direction facing = save.apply(obj);
                if (facing != null) {
                    final byte b = (byte) facing.getIndex();
                    return new NBTTagByte(b);
                }
                return null;
            },
            (obj, data) -> {
                byte b = data.getByte();
                Direction facing = Direction.getFront(b);
                load.accept(obj, facing);
            }
        );
    }
}
