package icbm.classic.mods.cc;

import dan200.computercraft.api.ComputerCraftAPI;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.api.peripheral.IPeripheralProvider;
import dan200.computercraft.api.peripheral.IPeripheralTile;
import icbm.classic.ICBMConstants;
import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.content.blocks.launcher.base.TileLauncherBase;
import icbm.classic.content.blocks.launcher.cruise.TileCruiseLauncher;
import icbm.classic.content.blocks.radarstation.TileRadarStation;
import icbm.classic.mods.ModProxy;
import icbm.classic.mods.cc.builder.PeripheralBuilder;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Optional;

import javax.annotation.Nonnull;

@Optional.InterfaceList({
    @Optional.Interface(iface = "dan200.computercraft.api.peripheral.IPeripheralProvider", modid = "computercraft")
})
public class CCProxy extends ModProxy implements IPeripheralProvider {
    public static final CCProxy INSTANCE = new CCProxy();



    private final PeripheralBuilder<TileRadarStation> radarBuilder = new PeripheralBuilder<TileRadarStation>(ICBMConstants.PREFIX + "radar.station")
        .withMethod("getBattery", (radar -> new Object[]{radar.getTile().getEnergy(), radar.getTile().getEnergyBufferSize(), radar.getTile().getEnergyConsumption()}));

    @Override
    public void init() {
        ComputerCraftAPI.registerPeripheralProvider(this);
    }

    @Override
    @Optional.Method(modid = "computercraft")
    public IPeripheral getPeripheral(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull EnumFacing side) {
        final TileEntity tile = world.getTileEntity(pos);
        final EnumFacing connectedSide = side.getOpposite();
        if(tile == null) {
            return null;
        }
        else if(tile instanceof IPeripheralTile) {
            return ((IPeripheralTile) tile).getPeripheral(side);
        }
        else if(tile instanceof TileRadarStation) {
            return radarBuilder.build((TileRadarStation) tile, connectedSide);
        }
        else if(tile instanceof TileLauncherBase) {
            return new LauncherBasePeripheral((TileLauncherBase) tile, ((TileLauncherBase) tile).missileLauncher, connectedSide);
        }
        else if(tile instanceof TileCruiseLauncher) {
            return new LauncherCruisePeripheral((TileCruiseLauncher) tile, ((TileCruiseLauncher) tile).launcher, connectedSide);
        }
        else if (tile.hasCapability(ICBMClassicAPI.MISSILE_LAUNCHER_CAPABILITY, connectedSide)) {
            return new LauncherPeripheral(tile, tile.getCapability(ICBMClassicAPI.MISSILE_LAUNCHER_CAPABILITY, connectedSide), connectedSide);
        }
        return null;
    }
}
