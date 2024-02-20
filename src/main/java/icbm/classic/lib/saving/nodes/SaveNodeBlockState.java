package icbm.classic.lib.saving.nodes;

import icbm.classic.lib.saving.NbtSaveNode;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;

import java.util.function.BiConsumer;
import java.util.function.Function;

public class SaveNodeBlockState<E> extends NbtSaveNode<E, NBTTagCompound>
{
    public SaveNodeBlockState(String name, Function<E, IBlockState> save, BiConsumer<E, IBlockState> load)
    {
        super(name,
            (obj) -> {
                final IBlockState blockState = save.apply(obj);
                if (blockState != null)
                {
                    return NBTUtil.writeBlockState(new NBTTagCompound(), blockState);
                }
                return null;
            },
            (obj, data) -> load.accept(obj, NBTUtil.readBlockState(data))
        );
    }
}
