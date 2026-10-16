package icbm.classic.lib.saving.nodes;

import icbm.classic.lib.saving.NbtSaveNode;
import net.minecraft.nbt.ByteTag;
import net.minecraft.core.Direction;

import java.util.function.BiConsumer;
import java.util.function.Function;

public class SaveNodeFacing<E> extends NbtSaveNode<E, ByteTag> //TODO convert to enum save/load (small enums can do short, large can do ints)
{
    public SaveNodeFacing(String name, Function<E, Direction> save, BiConsumer<E, Direction> load)
    {
        super(name,
            (obj) -> save(save.apply(obj)),
            (obj, data) -> load.accept(obj, load(data))
        );
    }

    public static ByteTag save(Direction facing) {
        if (facing != null)
        {
            final byte b = (byte) facing.get3DDataValue();
            return ByteTag.valueOf(b);
        }
        return null;
    }
    public static Direction load(ByteTag save) {
        return Direction.from3DDataValue(save.getAsByte());
    }
}
