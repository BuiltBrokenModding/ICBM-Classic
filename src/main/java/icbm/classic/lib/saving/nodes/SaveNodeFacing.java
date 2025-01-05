package icbm.classic.lib.saving.nodes;

import icbm.classic.lib.saving.NbtSaveNode;
import net.minecraft.nbt.ByteNBT;
import net.minecraft.util.Direction;

import java.util.function.BiConsumer;
import java.util.function.Function;

public class SaveNodeFacing<E> extends NbtSaveNode<E, ByteNBT> //TODO convert to enum save/load (small enums can do short, large can do ints)
{
    public SaveNodeFacing(String name, Function<E, Direction> save, BiConsumer<E, Direction> load)
    {
        super(name,
            (obj) -> save(save.apply(obj)),
            (obj, data) -> load.accept(obj, load(data))
        );
    }

    public static ByteNBT save(Direction facing) {
        if (facing != null)
        {
            final byte b = (byte) facing.getIndex();
            return new ByteNBT(b);
        }
        return null;
    }
    public static Direction load(ByteNBT save) {
        return Direction.byIndex(save.getByte());
    }
}
