package icbm.classic.mods.cc;

import dan200.computercraft.api.lua.ArgumentHelper;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;
import icbm.classic.ICBMConstants;
import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.caps.IMissileHolder;
import icbm.classic.api.launcher.IActionStatus;
import icbm.classic.api.launcher.IMissileLauncher;
import icbm.classic.api.missiles.ICapabilityMissileStack;
import icbm.classic.api.missiles.cause.IMissileCause;
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

    public static final String[] methods = new String[]{"getMissiles", "launch"};

    protected static final int METHOD_MISSILES_GET = 0;
    protected static final int METHOD_LAUNCH = 1;

    public final IMissileLauncher launcher;
    public final EnumFacing side;

    public LauncherPeripheral(T tile, IMissileLauncher launcher, EnumFacing side) {
        super(tile);
        this.launcher = launcher;
        this.side = side;
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
        else if (method == METHOD_LAUNCH) {
            final Map<?, ?> table = ArgumentHelper.getTable(objects, 0);
            final boolean simulate = ArgumentHelper.getBoolean(objects, 1);

            final double x = getNumeric(table, "x", "Error: failed to get x value...");
            final double y = getNumeric(table, "y", "Error: failed to get y value...");
            final double z = getNumeric(table, "z", "Error: failed to get z value...");
            final int delay = (int)Math.floor(getNumeric(table, "delay", "Error: failed to get delay value..."));

            return iLuaContext.executeMainThreadTask(() -> {
                final BasicTargetData targetData = new BasicTargetData(x, y, z).withFiringDelay(delay);
                final BlockPos sourceBlock = tile.getPos().offset(side);
                final IMissileCause cause = new BlockCause(tile.getWorld(), sourceBlock, tile.getWorld().getBlockState(sourceBlock)); //TODO make custom cause and try to log OS owner
                final IActionStatus status = launcher.launch(targetData, cause, simulate);

                return out(status.isError(), status.shouldBlockInteraction(), status.getRegistryName().toString(), status.message().getFormattedText());
            });
        }
        return null;
    }
}
