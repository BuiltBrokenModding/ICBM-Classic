package icbm.classic.mods.cc;

import dan200.computercraft.api.ComputerCraftAPI;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.api.peripheral.IPeripheralProvider;
import icbm.classic.ICBMConstants;
import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.launcher.IMissileLauncher;
import icbm.classic.mods.ModProxy;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

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
            return new LauncherPeripheral(tile, tile.getCapability(ICBMClassicAPI.MISSILE_LAUNCHER_CAPABILITY, side.getOpposite()));
        }
        return null;
    }

    public static class LauncherPeripheral implements IPeripheral {

        public static final String[] methods = new String[] {"hasMissile"};

        public final TileEntity tile;
        public final IMissileLauncher launcher;

        public LauncherPeripheral(TileEntity tile, IMissileLauncher launcher) {
            this.tile = tile;
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
            if(method == 0) {
                return new Object[]{true};
            }
            return null;
        }

        @Override
        public boolean equals(@Nullable IPeripheral iPeripheral) {
            return iPeripheral instanceof LauncherPeripheral && ((LauncherPeripheral) iPeripheral).tile == this.tile;
        }
    }
}
