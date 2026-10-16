package icbm.classic.lib.saving.nodes;

import icbm.classic.lib.saving.NbtSaveNode;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.ResourceLocation;

import java.util.function.BiConsumer;
import java.util.function.Function;

public class SaveNodeResourceLocation<E> extends NbtSaveNode<E, StringTag>
{
    public SaveNodeResourceLocation(final String name, Function<E, ResourceLocation> save, BiConsumer<E, ResourceLocation> load)
    {
        super(name,
            (obj) -> SaveNodeResourceLocation.save(save.apply(obj)),
            (obj, data) -> {
                load.accept(obj, SaveNodeResourceLocation.load(data));
            }
        );
    }

    public static StringTag save(ResourceLocation key) {
        if(key == null) {
            return null;
        }
        return StringTag.valueOf(key.toString());
    }

    public static ResourceLocation load(StringTag data) {
        return ResourceLocation.parse(data.getAsString());
    }
}
