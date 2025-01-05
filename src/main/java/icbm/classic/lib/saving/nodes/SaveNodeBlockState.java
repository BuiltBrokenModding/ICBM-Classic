package icbm.classic.lib.saving.nodes;

import icbm.classic.lib.saving.NbtSaveNode;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;

import java.util.function.BiConsumer;
import java.util.function.Function;

public class SaveNodeBlockState<E> extends NbtSaveNode<E, CompoundNBT>
{
    public SaveNodeBlockState(String name, Function<E, BlockState> save, BiConsumer<E, BlockState> load)
    {
        super(name,
            (obj) -> {
                final BlockState blockState = save.apply(obj);
                if (blockState != null)
                {
                    return NBTUtil.writeBlockState(blockState);
                }
                return null;
            },
            (obj, data) -> load.accept(obj, NBTUtil.readBlockState(data))
        );
    }
}
