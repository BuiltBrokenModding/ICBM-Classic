package icbm.classic.mods.cc.builder;

import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import lombok.Data;
import net.minecraft.tileentity.TileEntity;

import javax.annotation.Nonnull;

@Data
public abstract class PeripheralMethod<T extends TileEntity> {
    private final String name;

    public abstract Object[] invoke(@Nonnull Peripheral<T> peripheral, @Nonnull IComputerAccess computer, @Nonnull ILuaContext context, @Nonnull Object[] args) throws LuaException, InterruptedException;
}
