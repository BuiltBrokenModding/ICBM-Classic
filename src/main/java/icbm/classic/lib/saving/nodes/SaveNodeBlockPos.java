package icbm.classic.lib.saving.nodes;

import icbm.classic.lib.saving.NbtSaveNode;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;

import java.util.function.BiConsumer;
import java.util.function.Function;

public class SaveNodeBlockPos<E> extends NbtSaveNode<E, NBTTagCompound>
{
    public SaveNodeBlockPos(final String name, Function<E, BlockPos> save, BiConsumer<E, BlockPos> load) {
        super(name,
            (obj) -> save(save.apply(obj)),
            (obj, data) -> load.accept(obj, load(data))
        );
    }

    public static NBTTagCompound save(BlockPos pos) {
        if(pos != null) {
            final NBTTagCompound compound = new NBTTagCompound();
            compound.setInteger("x", pos.getX());
            compound.setInteger("y", pos.getY());
            compound.setInteger("z", pos.getZ());
            return compound;
        }
        return null;
    }

    public static BlockPos load(NBTTagCompound data) {
        return new BlockPos(
            data.getInteger("x"),
            data.getInteger("y"),
            data.getInteger("z")
        );
    }
}
