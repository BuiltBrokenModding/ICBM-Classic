package icbm.classic.lib.saving.nodes;

import icbm.classic.api.missiles.parts.IBuildableObject;
import icbm.classic.api.reg.obj.IBuilderRegistry;
import icbm.classic.lib.saving.NbtSaveNode;
import net.minecraft.nbt.NBTTagCompound;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class SaveBuildableObject<E, C extends IBuildableObject> extends NbtSaveNode<E, NBTTagCompound>  {
    public SaveBuildableObject(String name, final IBuilderRegistry<C> reg, Function<E, C> getter, BiConsumer<E, C> setter) {
        super(name,
            (source) -> Optional.ofNullable(getter.apply(source)).map(reg::save).orElse(null),
            (source, data) -> {
                final C object = reg.load(data);
                if(object != null) {
                    setter.accept(source, object);
                }
            });
    }
}
