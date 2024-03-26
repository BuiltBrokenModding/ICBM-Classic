package icbm.classic.lib.saving.nodes;

import icbm.classic.lib.saving.NbtSaveNode;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.Vec3;

import java.util.function.BiConsumer;
import java.util.function.Function;

public class SaveNodeVec3<E> extends NbtSaveNode<E, CompoundTag> {
    public SaveNodeVec3(final String name, Function<E, Vec3> getter, BiConsumer<E, Vec3> setter) {
        super(name,
            (obj) -> {
                final Vec3 pos = getter.apply(obj);
                if (pos != null) {
                    return save(pos);
                }
                return null;
            },
            (obj, data) -> {
                setter.accept(obj, load(data));
            }
        );
    }

    public static CompoundTag save(Vec3 pos) {
        return save(pos, new CompoundTag());
    }

    public static CompoundTag save(Vec3 pos, CompoundTag compound) {
        compound.setDouble("x", pos.x);
        compound.setDouble("y", pos.y);
        compound.setDouble("z", pos.z);
        return compound;
    }

    public static Vec3 load(CompoundTag compound) {
        return new Vec3(
            compound.getDouble("x"),
            compound.getDouble("y"),
            compound.getDouble("z")
        );
    }
}
