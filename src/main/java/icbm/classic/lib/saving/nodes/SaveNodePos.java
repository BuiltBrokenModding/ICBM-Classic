package icbm.classic.lib.saving.nodes;

import icbm.classic.lib.saving.NbtSaveNode;
import icbm.classic.lib.transform.vector.Pos;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;

import java.util.function.BiConsumer;
import java.util.function.Function;

@Deprecated
public class SaveNodePos<E> extends NbtSaveNode<E, NBTTagCompound>
{
    public SaveNodePos(final String name, Function<E, Pos> save, BiConsumer<E, Pos> load) {
        super(name,
            (obj) -> {
                final Pos pos = save.apply(obj);
                if (pos != null)
                {
                    final NBTTagCompound compound = new NBTTagCompound();
                    compound.setDouble("x", pos.getX());
                    compound.setDouble("y", pos.getY());
                    compound.setDouble("z", pos.getZ());
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
