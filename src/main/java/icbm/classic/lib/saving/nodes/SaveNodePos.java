package icbm.classic.lib.saving.nodes;

import icbm.classic.lib.saving.NbtSaveNode;
import icbm.classic.lib.transform.vector.Pos;
import net.minecraft.nbt.CompoundNBT;

import java.util.function.BiConsumer;
import java.util.function.Function;

@Deprecated
public class SaveNodePos<E> extends NbtSaveNode<E, CompoundNBT>
{
    public SaveNodePos(final String name, Function<E, Pos> save, BiConsumer<E, Pos> load) {
        super(name,
            (obj) -> {
                final Pos pos = save.apply(obj);
                if (pos != null)
                {
                    final CompoundNBT compound = new CompoundNBT();
                    compound.putDouble("x", pos.getX());
                    compound.putDouble("y", pos.getY());
                    compound.putDouble("z", pos.getZ());
                    return compound;
                }
                return null;
            },
            (obj, data) -> {
                final Pos pos = new Pos(
                    data.getDouble("x"),
                    data.getDouble("y"),
                    data.getDouble("z")
                );
                load.accept(obj, pos);
            }
        );
    }
}
