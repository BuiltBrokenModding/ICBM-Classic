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
import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Supplier;

public class TileReg {
    public static final DeferredRegister<TileEntityType<?>> TILES = new DeferredRegister<>(ForgeRegistries.TILE_ENTITIES, ICBMConstants.DOMAIN);

    public static final RegistryObject<TileEntityType<TileEMPTower>> EMP_TOWER_BASE = register(
        TileEMPTower.REGISTRY_NAME,
        BlockReg.EMP_TOWER_BASE,
        TileEMPTower::new
    );
    public static final RegistryObject<TileEntityType<TileEmpTowerFake>> EMP_TOWER_COIL = register(
        TileEmpTowerFake.REGISTRY_NAME,
        BlockReg.EMP_TOWER_COIL,
        TileEmpTowerFake::new
    );

    public static final RegistryObject<TileEntityType<TileRadarStation>> RADAR_SCREEN = register(
        TileRadarStation.REGISTRY_NAME,
        BlockReg.RADAR_SCREEN,
        TileRadarStation::new
    );

    public static final RegistryObject<TileEntityType<TileLauncherFrame>> LAUNCHER_FRAME = register(
        TileLauncherFrame.REGISTRY_NAME,
        BlockReg.LAUNCHER_FRAME,
        TileLauncherFrame::new
    );
    public static final RegistryObject<TileEntityType<TileLauncherConnector>> LAUNCHER_CONNECTOR = register(
        TileLauncherConnector.REGISTRY_NAME,
        BlockReg.LAUNCHER_CONNECTOR,
        TileLauncherConnector::new
    );
    public static final RegistryObject<TileEntityType<TileLauncherBase>> LAUNCHER_BASE = register(
        TileLauncherBase.REGISTRY_NAME,
        BlockReg.LAUNCHER_BASE,
        TileLauncherBase::new
    );
    public static final RegistryObject<TileEntityType<TileLauncherScreen>> LAUNCHER_SCREEN = register(
        TileLauncherScreen.REGISTRY_NAME,
        BlockReg.LAUNCHER_SCREEN,
        TileLauncherScreen::new
    );
    public static final RegistryObject<TileEntityType<TileCruiseLauncher>> LAUNCHER_CRUISE = register(
        TileCruiseLauncher.REGISTRY_NAME,
        BlockReg.LAUNCHER_CRUISE,
        TileCruiseLauncher::new
    );

    private static <TILE extends TileEntity> RegistryObject<TileEntityType<TILE>> register(
        ResourceLocation name, Supplier<Block> blockSupplier, Supplier<TILE> factory) {
        //TODO pull resource location from block to keep things consistent
        return TILES.register(
            name.getPath(),
            () -> TileEntityType.Builder.create(factory, blockSupplier.get()).build(null)
        );
    }
}
