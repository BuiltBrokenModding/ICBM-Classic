package icbm.classic.lib.saving.nodes;

import icbm.classic.lib.saving.NbtSaveNode;
import icbm.classic.lib.RegistryHelper;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;

import java.util.function.BiConsumer;
import java.util.function.Function;

public class SaveNodeBlockState<E> extends NbtSaveNode<E, CompoundTag>
{
    public SaveNodeBlockState(String name, Function<E, BlockState> save, BiConsumer<E, BlockState> load)
    {
        super(name,
            (obj) -> {
                final BlockState blockState = save.apply(obj);
                if (blockState != null)
                {
                    return NbtUtils.writeBlockState(blockState);
                }
                return null;
            },
            (obj, data) -> load.accept(obj, NbtUtils.readBlockState(RegistryHelper.access().lookupOrThrow(Registries.BLOCK), data))
        );
    }
}
