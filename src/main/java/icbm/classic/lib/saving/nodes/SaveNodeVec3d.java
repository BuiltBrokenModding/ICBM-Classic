package icbm.classic.lib.saving.nodes;

import icbm.classic.lib.saving.NbtSaveNode;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.Vec3d;

import java.util.function.BiConsumer;
import java.util.function.Function;

public class SaveNodeVec3d<E> extends NbtSaveNode<E, NBTTagCompound>
{
    public SaveNodeVec3d(final String name, Function<E, Vec3d> getter, BiConsumer<E, Vec3d> setter) {
        super(name,
            (obj) -> {
                final Vec3d pos = getter.apply(obj);
                if (pos != null)
                {
                    return save(pos);
                }
                return null;
            },
            (obj, data) -> {
                setter.accept(obj, load(data));
            }
        );
    }

    public static NBTTagCompound save(Vec3d pos) {
        return save(pos, new NBTTagCompound());
    }
    public static NBTTagCompound save(Vec3d pos, NBTTagCompound compound) {
        compound.setDouble("x", pos.x);
        compound.setDouble("y", pos.y);
        compound.setDouble("z", pos.z);
        return compound;
    }

    public static Vec3d load(NBTTagCompound compound) {
        return new Vec3d(
            compound.getDouble("x"),
            compound.getDouble("y"),
            compound.getDouble("z")
        );
    }
}
