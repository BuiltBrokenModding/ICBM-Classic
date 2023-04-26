package icbm.classic.mods.cc;

import dan200.computercraft.api.ComputerCraftAPI;
import dan200.computercraft.api.lua.ArgumentHelper;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.api.peripheral.IPeripheralProvider;
import icbm.classic.ICBMClassic;
import icbm.classic.ICBMConstants;
import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.caps.IMissileHolder;
import icbm.classic.api.launcher.IActionStatus;
import icbm.classic.api.launcher.IMissileLauncher;
import icbm.classic.api.missiles.ICapabilityMissileStack;
import icbm.classic.api.missiles.cause.IMissileCause;
import icbm.classic.content.missile.logic.source.cause.BlockCause;
import icbm.classic.content.missile.logic.targeting.BasicTargetData;
import icbm.classic.mods.ModProxy;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.BiFunction;
import java.util.function.Function;

@Optional.InterfaceList({
    @Optional.Interface(iface = "dan200.computercraft.api.peripheral.IPeripheralProvider", modid = "computercraft")
})
public class CCProxy extends ModProxy implements IPeripheralProvider {
    public static final CCProxy INSTANCE = new CCProxy();

    @Override
    public void init() {
        ComputerCraftAPI.registerPeripheralProvider(this);
    }

    @Override
    @Optional.Method(modid = "computercraft")
    public IPeripheral getPeripheral(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull EnumFacing side) {
        final TileEntity tile = world.getTileEntity(pos);
        if (tile != null && tile.hasCapability(ICBMClassicAPI.MISSILE_LAUNCHER_CAPABILITY, side.getOpposite())) {
            return new LauncherPeripheral(tile, tile.getCapability(ICBMClassicAPI.MISSILE_LAUNCHER_CAPABILITY, side.getOpposite()), side.getOpposite());
        }
        return null;
    }

    public static class LauncherPeripheral implements IPeripheral {

        public static final String[] methods = new String[] {"missiles", "launch"};

        public final TileEntity tile;
        public final IMissileLauncher launcher;
        public final EnumFacing side;

        public LauncherPeripheral(TileEntity tile, IMissileLauncher launcher, EnumFacing side) {
            this.tile = tile;
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

            // missiles
            if(method == 0) {
                if(tile.hasCapability(ICBMClassicAPI.MISSILE_HOLDER_CAPABILITY, side)) {
                    final IMissileHolder holder = tile.getCapability(ICBMClassicAPI.MISSILE_HOLDER_CAPABILITY, side);
                    if(holder != null) {
                        final ItemStack missileStack = holder.getMissileStack();
                        if(missileStack.isEmpty()) {
                            return new Object[]{"empty"};
                        }
                        else if(missileStack.hasCapability(ICBMClassicAPI.MISSILE_STACK_CAPABILITY, side)) {
                            final ICapabilityMissileStack stackCap = missileStack.getCapability(ICBMClassicAPI.MISSILE_STACK_CAPABILITY, side);
                            if(stackCap != null) {
                                return new Object[]{stackCap.getMissileId()};
                            }
                        }
                        return new Object[]{missileStack.toString()};
                    }
                }
                return new Object[]{null};
            }
            // launch
            else if(method == 1) {
                final double x = ArgumentHelper.getDouble(objects, 0);
                final double y = ArgumentHelper.getDouble(objects, 1);
                final double z = ArgumentHelper.getDouble(objects, 2);
                final boolean simulate = ArgumentHelper.getBoolean(objects, 3);
                return iLuaContext.executeMainThreadTask(() -> {
                    final BasicTargetData targetData = new BasicTargetData(x, y, z);
                    final BlockPos sourceBlock = tile.getPos().offset(side);
                    final IMissileCause cause = new BlockCause(tile.getWorld(), sourceBlock, tile.getWorld().getBlockState(sourceBlock)); //TODO make custom cause and try to log OS owner
                    final IActionStatus status = launcher.launch(targetData, cause, simulate);

                    return new Object[]{status.isError(), status.shouldBlockInteraction(), status.getRegistryName().toString(), status.message().getFormattedText()};
                });
            }
            return null;
        }

        @Override
        public boolean equals(@Nullable IPeripheral iPeripheral) {
            return iPeripheral instanceof LauncherPeripheral && ((LauncherPeripheral) iPeripheral).tile == this.tile;
        }
    }
}
