package icbm.classic.content.reg;

import icbm.classic.ICBMConstants;
import icbm.classic.content.blocks.emptower.TileEMPTower;
import icbm.classic.content.blocks.emptower.TileEmpTowerFake;
import icbm.classic.content.blocks.launcher.base.TileLauncherBase;
import icbm.classic.content.blocks.launcher.connector.TileLauncherConnector;
import icbm.classic.content.blocks.launcher.cruise.TileCruiseLauncher;
import icbm.classic.content.blocks.launcher.frame.TileLauncherFrame;
import icbm.classic.content.blocks.launcher.screen.TileLauncherScreen;
import icbm.classic.content.blocks.radarstation.TileRadarStation;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class TileReg
{
    public static final DeferredRegister<TileEntityType<?>> TILES = new DeferredRegister<>(ForgeRegistries.TILE_ENTITIES, ICBMConstants.DOMAIN);

    public static final RegistryObject<TileEntityType<?>> EMP_TOWER_BASE = TILES.register(TileEMPTower.REGISTRY_NAME.getPath(), () -> TileEntityType.Builder.create(TileEMPTower::new, BlockReg.EMP_TOWER_BASE.get()).build(null));
    public static final RegistryObject<TileEntityType<?>> EMP_TOWER_COIL = TILES.register(TileEmpTowerFake.REGISTRY_NAME.getPath(), () -> TileEntityType.Builder.create(TileEmpTowerFake::new, BlockReg.EMP_TOWER_COIL.get()).build(null));

    public static final RegistryObject<TileEntityType<?>> RADAR_SCREEN = TILES.register(TileRadarStation.REGISTRY_NAME.getPath(), () -> TileEntityType.Builder.create(TileRadarStation::new, BlockReg.RADAR_SCREEN.get()).build(null));

    public static final RegistryObject<TileEntityType<?>> LAUNCHER_FRAME = TILES.register(TileLauncherFrame.REGISTRY_NAME.getPath(), () -> TileEntityType.Builder.create(TileLauncherFrame::new, BlockReg.LAUNCHER_FRAME.get()).build(null));
    public static final RegistryObject<TileEntityType<?>> LAUNCHER_CONNECTOR = TILES.register(TileLauncherConnector.REGISTRY_NAME.getPath(), () -> TileEntityType.Builder.create(TileLauncherConnector::new, BlockReg.LAUNCHER_CONNECTOR.get()).build(null));
    public static final RegistryObject<TileEntityType<?>> LAUNCHER_BASE = TILES.register(TileLauncherBase.REGISTRY_NAME.getPath(), () -> TileEntityType.Builder.create(TileLauncherBase::new, BlockReg.LAUNCHER_BASE.get()).build(null));
    public static final RegistryObject<TileEntityType<?>> LAUNCHER_SCREEN = TILES.register(TileLauncherScreen.REGISTRY_NAME.getPath(), () -> TileEntityType.Builder.create(TileLauncherScreen::new, BlockReg.LAUNCHER_SCREEN.get()).build(null));
    public static final RegistryObject<TileEntityType<?>> LAUNCHER_CRUISE = TILES.register(TileCruiseLauncher.REGISTRY_NAME.getPath(), () -> TileEntityType.Builder.create(TileCruiseLauncher::new, BlockReg.LAUNCHER_CRUISE.get()).build(null));
}
