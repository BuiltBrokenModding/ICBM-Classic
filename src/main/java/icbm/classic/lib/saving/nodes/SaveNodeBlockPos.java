package icbm.classic.lib.saving.nodes;

import icbm.classic.lib.saving.NbtSaveNode;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.BlockPos;

import java.util.function.BiConsumer;
import java.util.function.Function;

public class SaveNodeBlockPos<E> extends NbtSaveNode<E, CompoundTag>
{
    public SaveNodeBlockPos(final String name, Function<E, BlockPos> save, BiConsumer<E, BlockPos> load) {
        super(name,
            (obj) -> save(save.apply(obj)),
            (obj, data) -> load.accept(obj, load(data))
        );
    }

    public static CompoundTag save(BlockPos pos) {
        if(pos != null) {
            final CompoundTag compound = new CompoundTag();
            compound.putInt("x", pos.getX());
            compound.putInt("y", pos.getY());
            compound.putInt("z", pos.getZ());
            return compound;
        }
        return null;
    }

    public static BlockPos load(CompoundTag data) {
        return new BlockPos(
            data.getInt("x"),
            data.getInt("y"),
            data.getInt("z")
        );
    }
}
