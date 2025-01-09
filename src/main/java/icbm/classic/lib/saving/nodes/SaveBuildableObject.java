package icbm.classic.lib.saving.nodes;

import icbm.classic.ICBMClassic;
import icbm.classic.api.reg.obj.IBuildableObject;
import icbm.classic.api.reg.obj.IBuilderRegistry;
import icbm.classic.lib.saving.NbtSaveNode;
import net.minecraft.nbt.CompoundNBT;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class SaveBuildableObject<E, C extends IBuildableObject> extends NbtSaveNode<E, CompoundNBT>  {
    public SaveBuildableObject(String name, final Supplier<IBuilderRegistry<C>> builderRegistry, Function<E, C> getter, BiConsumer<E, C> setter) {
        super(name,
            (source) -> {
                final C object = getter.apply(source);
                if(object != null) {
                    if(builderRegistry.get() == null){
                        ICBMClassic.logger().error("Failed to save buildable object node, builder registry is null...field:{}",name);
                        return null;
                    }
                    return builderRegistry.get().save(object);
                }
                return null;
            },
            (source, data) -> {
                if(builderRegistry.get() == null){
                    ICBMClassic.logger().error("Failed to load buildable object node, builder registry is null...field:{}",name);
                    return;
                }
                final C object = builderRegistry.get().load(data);
                if(object != null) {
                    setter.accept(source, object);
                }
            });
    }
}
