package icbm.classic.mods.cc.builder;

import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import net.minecraft.tileentity.TileEntity;

import javax.annotation.Nonnull;

@FunctionalInterface
public interface MethodFuncGet<T extends TileEntity> {

    Object[] apply(@Nonnull Peripheral<T> peripheral) throws LuaException, InterruptedException;
}
