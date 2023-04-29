package icbm.classic.mods.cc.builder;


import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

import java.util.ArrayList;
import java.util.List;

public class PeripheralBuilder<T extends TileEntity> {


    private final String type;
    private final List<PeripheralMethod<T>> methods = new ArrayList<>();

    public PeripheralBuilder(final String type) {
        this.type = type;
    }

    public PeripheralBuilder<T> withMethod(PeripheralMethod<T> method) {
        methods.add(method);
        return this;
    }

    public PeripheralBuilder<T> withMethod(String type, MethodFuncContext<T> method) {
        methods.add(new PeripheralMethodFunc<T>(type, method));
        return this;
    }

    public PeripheralBuilder<T> withMethod(String type, MethodFuncArgs<T> method) {
        methods.add(new PeripheralMethodFunc<T>(type, (peripheral, computer, context, args) -> method.apply(peripheral, args)));
        return this;
    }

    public PeripheralBuilder<T> withMethod(String type, MethodFuncGet<T> method) {
        methods.add(new PeripheralMethodFunc<T>(type, (peripheral, computer, context, args) -> method.apply(peripheral)));
        return this;
    }

    public String[] gatherMethodNames() {
        final String[] names = new String[methods.size()];
        for(int index = 0; index < methods.size(); index++) {
            names[index] = methods.get(index).getName();
        }
        return names;
    }

    public Peripheral<T> build(T tile, EnumFacing side) {
        return new Peripheral<T>(type, gatherMethodNames(), methods, tile, tile.getPos().offset(side), side);
    }
}
