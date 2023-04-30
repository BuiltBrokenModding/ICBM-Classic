package icbm.classic.mods.cc.builder;

import dan200.computercraft.api.lua.LuaException;
import net.minecraft.tileentity.TileEntity;

import javax.annotation.Nonnull;

@FunctionalInterface
public interface MethodFuncArgs<T extends TileEntity> {

    Object[] apply(@Nonnull Peripheral<T> peripheral, @Nonnull Object[] args) throws LuaException, InterruptedException;
}
