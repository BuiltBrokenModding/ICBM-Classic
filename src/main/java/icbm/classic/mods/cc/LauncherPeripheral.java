package icbm.classic.mods.cc;

import dan200.computercraft.api.lua.ArgumentHelper;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import icbm.classic.ICBMConstants;
import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.caps.IMissileHolder;
import icbm.classic.api.launcher.IActionStatus;
import icbm.classic.api.launcher.IMissileLauncher;
import icbm.classic.api.missiles.ICapabilityMissileStack;
import icbm.classic.api.missiles.cause.IMissileCause;
import icbm.classic.api.missiles.parts.IMissileTarget;
import icbm.classic.content.missile.logic.source.cause.BlockCause;
import icbm.classic.content.missile.logic.targeting.BasicTargetData;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

public class LauncherPeripheral<T extends TileEntity> extends CCPeripheral<T> {

    public static final String[] methods = new String[]{"getMissiles", "launch", "getInaccuracy", "getStatus", "preCheckLaunch"};

    protected static final int METHOD_MISSILES_GET = 0;
    protected static final int METHOD_LAUNCH = 1;
    protected static final int METHOD_INACCURACY_GET = 2;
    protected static final int METHOD_STATUS_GET = 3;
    protected static final int METHOD_LAUNCH_PRE_CHECK = 4;

    public final IMissileLauncher launcher;

    public LauncherPeripheral(T tile, IMissileLauncher launcher, EnumFacing side) {
        super(tile, side);
        this.launcher = launcher;
    }

    @Nonnull
    @Override
    public String getType() {
        return ICBMConstants.PREFIX + "launcher";
    }

    @Nonnull
    @Override
    public String[] getMethodNames() {
        return methods;
    }

    @Nullable
    @Override
    public Object[] callMethod(@Nonnull IComputerAccess iComputerAccess, @Nonnull ILuaContext iLuaContext, int method, @Nonnull Object[] objects) throws LuaException, InterruptedException {

        if (method == METHOD_MISSILES_GET) {
           return getMissiles();
        }
        else if (method == METHOD_LAUNCH) {
            return launchMissile(iLuaContext, objects);
        }
        else if (method == METHOD_INACCURACY_GET) {
            return getInaccuracy(objects);
        }
        else if(method == METHOD_STATUS_GET) {
            return convertStatus(launcher.getStatus());
        }
        else if(method == METHOD_LAUNCH_PRE_CHECK) {
            return preLaunchCheck(objects);
        }
        return null;
    }

    protected Object[] getMissiles() {
        if (tile.hasCapability(ICBMClassicAPI.MISSILE_HOLDER_CAPABILITY, side)) {
            final IMissileHolder holder = tile.getCapability(ICBMClassicAPI.MISSILE_HOLDER_CAPABILITY, side);
            if (holder != null) {
                final ItemStack missileStack = holder.getMissileStack();
                if (missileStack.isEmpty()) {
                    return out("empty");
                } else if (missileStack.hasCapability(ICBMClassicAPI.MISSILE_STACK_CAPABILITY, side)) {
                    final ICapabilityMissileStack stackCap = missileStack.getCapability(ICBMClassicAPI.MISSILE_STACK_CAPABILITY, side);
                    if (stackCap != null) {
                        return out(stackCap.getMissileId());
                    }
                }
                return out(missileStack.toString());
            }
        }
        return new Object[]{"not supported"}; //TODO maybe don't include in method list?
    }

    protected Object[] launchMissile(@Nonnull ILuaContext iLuaContext, @Nonnull Object[] objects) throws LuaException, InterruptedException {
        //TODO once ported over to addon, add a delay into the launcher network to force player to do launcherNetwork.fire(launchers) vs one at a time
        final Map<?, ?> table = ArgumentHelper.getTable(objects, 0);
        final boolean simulate = ArgumentHelper.getBoolean(objects, 1);
        final IMissileTarget targetData = getTarget(table);

        return iLuaContext.executeMainThreadTask(() -> {
            final IMissileCause cause = createLaunchCause();
            final IActionStatus status = launcher.launch(targetData, cause, simulate);
            return convertStatus(status);
        });
    }

    protected IMissileCause createLaunchCause() {
        final BlockPos sourceBlock = getComputerBlockPos();
        return new BlockCause(tile.getWorld(), sourceBlock, tile.getWorld().getBlockState(sourceBlock));
    }

    protected Object[] convertStatus(IActionStatus status) {
        return out(status.isError(), status.shouldBlockInteraction(), status.getRegistryName().toString(), status.message().getFormattedText());
    }

    protected IMissileTarget getTarget(Map<?, ?> table) throws LuaException {
        final double x = getNumeric(table, "x", "Error: failed to get x value...");
        final double y = getNumeric(table, "y", "Error: failed to get y value...");
        final double z = getNumeric(table, "z", "Error: failed to get z value...");
        final int delay = (int)Math.floor(getNumeric(table, "delay", "Error: failed to get delay value..."));
        return new BasicTargetData(x, y, z).withFiringDelay(delay);
    }

    protected Object[] getInaccuracy(@Nonnull Object[] objects) throws LuaException {
        final Map<?, ?> table = ArgumentHelper.getTable(objects, 0);
        final IMissileTarget targetData = getTarget(table);
        final int launcherCount = ArgumentHelper.optInt(objects, 1, 1);
        return out(launcher.getInaccuracy(targetData.getPosition(), launcherCount));
    }

    protected Object[] preLaunchCheck(@Nonnull Object[] objects) throws LuaException {
        final Map<?, ?> table = ArgumentHelper.getTable(objects, 0);
        final IMissileTarget targetData = getTarget(table);
        final IMissileCause cause = createLaunchCause();
        return convertStatus(launcher.preCheckLaunch(targetData, cause));
    }
}
