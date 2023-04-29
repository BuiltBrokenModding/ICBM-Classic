package icbm.classic.mods.cc;

import dan200.computercraft.api.lua.ArgumentHelper;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import icbm.classic.ICBMConstants;
import icbm.classic.api.launcher.IMissileLauncher;
import icbm.classic.content.blocks.launcher.base.TileLauncherBase;
import icbm.classic.content.blocks.launcher.cruise.TileCruiseLauncher;
import icbm.classic.lib.transform.rotation.EulerAngle;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.Vec3d;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class LauncherCruisePeripheral extends LauncherPeripheral<TileCruiseLauncher> {

    public static final String TYPE = ICBMConstants.PREFIX + "launcher.cruise";

    // First two methods are from super
    public static final String[] METHODS = new String[]{"getMissiles", "launch", "getInaccuracy", "getStatus", "preCheckLaunch", "getBattery", "getTarget", "setTarget", "isAimed", "getAimCurrent", "getAimTarget"};

    protected static final int METHOD_BATTERY_GET = 5;
    protected static final int METHOD_TARGET_GET = 6;
    protected static final int METHOD_TARGET_SET = 7;
    protected static final int METHOD_AIM_CHECK = 8;
    protected static final int METHOD_AIM_CURRENT = 9;
    protected static final int METHOD_AIM_TARGET = 10;

    public LauncherCruisePeripheral(TileCruiseLauncher tile, IMissileLauncher launcher, EnumFacing side) {
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
        if(method == METHOD_BATTERY_GET) {
            final int energy = tile.getEnergy();
            final int maxEnergy = tile.getEnergyBufferSize();
            final int firingCost = tile.getEnergyConsumption();
            return out(energy, maxEnergy, firingCost);
        }
        else if(method == METHOD_TARGET_GET) {
            final Vec3d target = tile.getTarget();
            return out(target.x, target.y, target.z);
        }
        else if(method == METHOD_TARGET_SET) {
            final double x = ArgumentHelper.getDouble(objects, 0);
            final double y = ArgumentHelper.getDouble(objects, 1);
            final double z = ArgumentHelper.getDouble(objects, 2);
            final Vec3d target = new Vec3d(x, y, z);
            return iLuaContext.executeMainThreadTask(() -> {
                tile.setTarget(target);
                return null;
            });
        }
        else if(method == METHOD_AIM_CHECK) {
            return out(tile.isAimed());
        }
        else if(method == METHOD_AIM_CURRENT || method == METHOD_AIM_TARGET) {
            final boolean toRadians = ArgumentHelper.optBoolean(objects, 0, false);
            final EulerAngle aim = method == METHOD_AIM_CURRENT ? tile.getCurrentAim() : tile.getAim();
            if(toRadians) {
                return out(aim.yaw_radian(), aim.pitch_radian(), aim.roll_radian());
            }
            return out(aim.yaw(), aim.pitch(), aim.roll());
        }

        //TODO add method for missile spawn point
        return super.callMethod(iComputerAccess, iLuaContext, method, objects);
    }
}
