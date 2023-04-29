package icbm.classic.mods.cc.builder;

import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import net.minecraft.tileentity.TileEntity;

import javax.annotation.Nonnull;

public class PeripheralMethodFunc<T extends TileEntity> extends PeripheralMethod<T> {

    private final MethodFunc<T> methodFunc;
    public PeripheralMethodFunc(String name, MethodFunc<T> methodFunc) {
        super(name);
        this.methodFunc = methodFunc;
    }

    @Override
    public Object[] invoke(@Nonnull Peripheral<T> peripheral, @Nonnull IComputerAccess computer, @Nonnull ILuaContext context, @Nonnull Object[] args) throws LuaException, InterruptedException {
        return methodFunc.apply(peripheral, computer, context, args);
    }
}
