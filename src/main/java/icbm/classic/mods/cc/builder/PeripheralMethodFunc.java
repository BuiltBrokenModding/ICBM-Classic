package icbm.classic.mods.cc.builder;

import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import net.minecraft.tileentity.TileEntity;

import javax.annotation.Nonnull;

public class PeripheralMethodFunc<T extends TileEntity> extends PeripheralMethod<T> {

    private final MethodFuncContext<T> methodFunc;
    public PeripheralMethodFunc(String name, MethodFuncContext<T> methodFuncContext) {
        super(name);
        this.methodFunc = methodFuncContext;
    }

    @Override
    public Object[] invoke(@Nonnull Peripheral<T> peripheral, @Nonnull IComputerAccess computer, @Nonnull ILuaContext context, @Nonnull Object[] args) throws LuaException, InterruptedException {
        return methodFunc.apply(peripheral, computer, context, args);
    }
}
