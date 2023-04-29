package icbm.classic.mods.cc.builder;

import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

/**
 * Generated peripheral for use in abstracting methods from the need
 * to build a new peripheral per tile. Allowing methods to be reused
 * or generated based on the target method.
 *
 * @param <T>
 */
@RequiredArgsConstructor
public class Peripheral<T extends TileEntity> implements IPeripheral {

    /** Unique id for the peripheral */
    @Getter
    private final String type;

    /** Method names for use in CC */
    @Getter
    private final String[] methodNames;

    /** Internal methods for wrapping, is same list in builder so don't edit */
    private final List<PeripheralMethod<T>> methods;

    /** Tile representing this peripheral */
    @Getter
    private final T tile;
    /** Suspected location of the computer access based on side. Useful mostly for logging and events. */
    @Getter
    private final BlockPos computerPos;
    /** Side of the tile accessed */
    @Getter
    private final EnumFacing accessedSide;

    @Nullable
    @Override
    public Object[] callMethod(@Nonnull IComputerAccess computer, @Nonnull ILuaContext context, int method, @Nonnull Object[] args) throws LuaException, InterruptedException {
        if(method >= 0 && method < methods.size()) {
            return methods.get(method).invoke(this, computer, context, args);
        }
        throw new LuaException("Unknown method");
    }

    @Override
    public boolean equals(@Nullable IPeripheral iPeripheral) {
        return iPeripheral instanceof Peripheral
            && Objects.equals(((Peripheral<?>) iPeripheral).type, type)
            && ((Peripheral<?>) iPeripheral).tile == tile;
    }
}
