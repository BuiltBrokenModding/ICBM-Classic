package icbm.classic.lib.saving.nodes;

import icbm.classic.lib.saving.NbtSaveNode;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.ResourceLocation;

import java.util.function.BiConsumer;
import java.util.function.Function;

public class SaveNodeResourceLocation<E> extends NbtSaveNode<E, NBTTagString>
{
    public SaveNodeResourceLocation(final String name, Function<E, ResourceLocation> save, BiConsumer<E, ResourceLocation> load)
    {
        super(name,
            (obj) -> {
                final ResourceLocation key = save.apply(obj);
                if(key == null) {
                    return null;
                }
                return new NBTTagString(key.toString());
            },
            (obj, data) -> {
                load.accept(obj, new ResourceLocation(data.getString()));
            }
        );
    }
}
