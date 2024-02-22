package icbm.classic.lib.saving.nodes;

import icbm.classic.lib.saving.NbtSaveNode;
import net.minecraft.nbt.NBTTagByte;
import net.minecraft.nbt.NBTTagString;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class SaveNodeEnum<SaveObject, EnumVal extends Enum<EnumVal>> extends NbtSaveNode<SaveObject, NBTTagString>
{
    public SaveNodeEnum(final String name, Function<SaveObject, EnumVal> save, BiConsumer<SaveObject, EnumVal> load, Function<String, EnumVal> accessor)
    {
        super(name,
            (obj) -> Optional.ofNullable(save.apply(obj)).map((b) -> new NBTTagString(b.name())).orElse(null),
            (obj, data) -> load.accept(obj, accessor.apply(data.getString()))
        );
    }
}
