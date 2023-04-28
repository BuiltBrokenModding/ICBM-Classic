package icbm.classic.mods.cc;

import dan200.computercraft.api.lua.ArgumentHelper;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import icbm.classic.ICBMConstants;
import icbm.classic.api.launcher.IMissileLauncher;
import icbm.classic.content.blocks.launcher.base.TileLauncherBase;
import net.minecraft.util.EnumFacing;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class LauncherBasePeripheral extends LauncherPeripheral<TileLauncherBase> {

    public static final String TYPE = ICBMConstants.PREFIX + "launcher.base";

    // First two methods are from super
    public static final String[] METHODS = new String[]{"getMissiles", "launch", "getLockHeight", "setLockHeight", "getFiringDelay", "setFiringDelay"};

    protected static final int METHOD_LOCK_HEIGHT_GET = 2;
    protected static final int METHOD_LOCK_HEIGHT_SET = 3;
    protected static final int METHOD_FIRING_DELAY_GET = 4;
    protected static final int METHOD_FIRING_DELAY_SET = 5;
    public LauncherBasePeripheral(TileLauncherBase tile, IMissileLauncher launcher, EnumFacing side) {
        super(tile, launcher, side);
    }

    @Nonnull
    @Override
    public String getType() {
        return TYPE;
    }

    @Nonnull
    @Override
    public String[] getMethodNames() {
        return METHODS;
    }

    @Nullable
    @Override
    public Object[] callMethod(@Nonnull IComputerAccess iComputerAccess, @Nonnull ILuaContext iLuaContext, int method, @Nonnull Object[] objects) throws LuaException, InterruptedException {
        if(method == METHOD_LOCK_HEIGHT_GET) {
            return out(tile.getLockHeight());
        }
        else if(method == METHOD_LOCK_HEIGHT_SET) {
            final int height = ArgumentHelper.getInt(objects, 0);
            return iLuaContext.executeMainThreadTask(() -> {
                tile.setLockHeight(height);
                return null;
            });
        }
        else if(method == METHOD_FIRING_DELAY_GET) {
            return out(tile.getFiringDelay());
        }
        else if(method == METHOD_FIRING_DELAY_SET) {
            final int delay = ArgumentHelper.getInt(objects, 0);
            return iLuaContext.executeMainThreadTask(() -> {
                tile.setFiringDelay(delay);
                return null;
            });
        }
        return super.callMethod(iComputerAccess, iLuaContext, method, objects);
    }
}
