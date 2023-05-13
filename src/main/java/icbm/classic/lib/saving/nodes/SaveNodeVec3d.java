package icbm.classic.lib.saving.nodes;

import icbm.classic.lib.saving.NbtSaveNode;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.Vec3d;

import java.util.function.BiConsumer;
import java.util.function.Function;

public class SaveNodeVec3d<E> extends NbtSaveNode<E, NBTTagCompound>
{
    public SaveNodeVec3d(final String name, Function<E, Vec3d> save, BiConsumer<E, Vec3d> load) {
        super(name,
            (obj) -> {
                final Vec3d pos = save.apply(obj);
                if (pos != null)
                {
                    final NBTTagCompound compound = new NBTTagCompound();
                    compound.setDouble("x", pos.x);
                    compound.setDouble("y", pos.y);
                    compound.setDouble("z", pos.z);
                    return compound;
                }
                return null;
            },
            (obj, data) -> {
                final Vec3d pos = new Vec3d(
                    data.getDouble("x"),
                    data.getDouble("y"),
                    data.getDouble("z")
                );
                load.accept(obj, pos);
            }
        );
    }
}
