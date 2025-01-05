package icbm.classic.lib.saving.nodes;

import icbm.classic.lib.saving.NbtSaveNode;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.Vec3d;

import java.util.function.BiConsumer;
import java.util.function.Function;

public class SaveNodeVec3d<E> extends NbtSaveNode<E, CompoundNBT>
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

    public static CompoundNBT save(Vec3d pos) {
        return save(pos, new CompoundNBT());
    }
    public static CompoundNBT save(Vec3d pos, CompoundNBT compound) {
        compound.putDouble("x", pos.x);
        compound.putDouble("y", pos.y);
        compound.putDouble("z", pos.z);
        return compound;
    }

    public static Vec3d load(CompoundNBT compound) {
        return new Vec3d(
            compound.getDouble("x"),
            compound.getDouble("y"),
            compound.getDouble("z")
        );
    }
}
