package icbm.classic.lib.saving.nodes;

import icbm.classic.api.reg.obj.IBuildableObject;
import icbm.classic.api.reg.obj.IBuilderRegistry;
import icbm.classic.lib.saving.NbtSaveNode;
import net.minecraft.nbt.ListNBT;

import java.util.Collection;
import java.util.function.Function;
import java.util.function.Supplier;

public class SaveBuildableObjectList<E, BUILDABLE extends IBuildableObject> extends NbtSaveNode<E, ListNBT>  {
    public <LIST extends Collection<BUILDABLE>> SaveBuildableObjectList(String name, final Supplier<IBuilderRegistry<BUILDABLE>> reg, Function<E, LIST> getter) {
        super(name,
            (source) -> {
                final LIST list = getter.apply(source);
                if(list != null && !list.isEmpty()) {
                    final ListNBT tagList = new ListNBT();
                    for(BUILDABLE obj : list) {
                        tagList.add(reg.get().save(obj));
                    }
                    return tagList;
                }
                return null;
            },
            (source, data) -> {
                final LIST object = getter.apply(source);
                if(object != null) {
                    object.clear();
                   for(int i = 0; i < data.size(); i++) {
                       object.add(reg.get().load(data.getCompound(i)));
                   }
                }
            });
    }
}
