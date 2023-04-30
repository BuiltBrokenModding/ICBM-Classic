package icbm.classic.mods.cc;

import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IPeripheral;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import java.util.Map;

public abstract class CCPeripheral<T extends TileEntity> implements IPeripheral {

    protected final T tile;
    protected final EnumFacing side;
    private final BlockPos computerPos;

    protected CCPeripheral(T tile, EnumFacing side) {
        this.tile = tile;
        this.side = side;
        this.computerPos = tile.getPos().offset(side);
    }

    protected Object[] out(Object... args) {
        return args;
    }

    protected BlockPos getComputerBlockPos() {
        return computerPos;
    }

    protected double getNumeric(Map<?, ?> table, String key, String errorPrefix) throws LuaException {

        if(!table.containsKey(key)) {
            throw new LuaException(errorPrefix + " not contained in table");
        }

        final Object value = table.get(key);

        if (!(value instanceof Number)) {
            throw new LuaException(errorPrefix + " not a numeric type");
        }
        final double d = ((Number) value).doubleValue();
        if (Double.isNaN(d)) {
            throw new LuaException(errorPrefix + " value is NaN");
        }
        if (!Double.isFinite(d)) {
            throw new LuaException(errorPrefix + " value is finite");
        }

        return d;
    }

    @Override
    public boolean equals(@Nullable IPeripheral iPeripheral) {
        return iPeripheral instanceof LauncherPeripheral && ((LauncherPeripheral) iPeripheral).tile == this.tile;
    }
}
