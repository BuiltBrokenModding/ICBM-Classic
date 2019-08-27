package icbm.classic.content.reg;

import icbm.classic.ICBMClassic;
import icbm.classic.content.blocks.*;
import icbm.classic.content.blocks.battery.BlockBattery;
import icbm.classic.content.blocks.battery.TileEntityBattery;
import icbm.classic.content.blocks.emptower.BlockEmpTower;
import icbm.classic.content.blocks.emptower.TileEMPTower;
import icbm.classic.content.blocks.explosive.BlockExplosive;
import icbm.classic.content.blocks.explosive.TileEntityExplosive;
import icbm.classic.content.blocks.launcher.base.BlockLauncherBase;
import icbm.classic.content.blocks.launcher.base.TileLauncherBase;
import icbm.classic.content.blocks.launcher.cruise.BlockCruiseLauncher;
import icbm.classic.content.blocks.launcher.cruise.TileCruiseLauncher;
import icbm.classic.content.blocks.launcher.frame.BlockLaunchFrame;
import icbm.classic.content.blocks.launcher.frame.TileLauncherFrame;
import icbm.classic.content.blocks.launcher.screen.BlockLaunchScreen;
import icbm.classic.content.blocks.launcher.screen.TileLauncherScreen;
import icbm.classic.content.blocks.multiblock.BlockMultiblock;
import icbm.classic.content.blocks.multiblock.TileMulti;
import icbm.classic.content.blocks.radarstation.BlockRadarStation;
import icbm.classic.content.blocks.radarstation.TileRadarStation;
import net.minecraft.block.Block;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

/**
 * Created by Dark(DarkGuardsman, Robert) on 1/7/19.
 */
@Mod.EventBusSubscriber(modid = ICBMClassic.DOMAIN)
public class BlockReg
{
    //TODO object holders
    public static Block blockGlassPlate;
    public static Block blockGlassButton;
    public static Block blockSpikes;
    public static Block blockCamo; //TODO re-implement
    public static Block blockConcrete;
    public static Block blockReinforcedGlass;
    public static Block blockExplosive;
    public static Block blockLaunchBase;
    public static Block blockLaunchScreen;
    public static Block blockLaunchSupport;
    public static Block blockRadarStation;
    public static Block blockEmpTower;
    public static Block blockCruiseLauncher; //TODO re-implement
    public static Block blockMissileCoordinator; //TODO re-implement
    public static Block blockBattery;
    public static Block multiBlock;

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event)
    {
        event.getRegistry().register(blockGlassPlate = new BlockGlassPressurePlate());
        event.getRegistry().register(blockGlassButton = new BlockGlassButton());
        event.getRegistry().register(blockSpikes = new BlockSpikes());
        event.getRegistry().register(blockConcrete = new BlockConcrete());
        event.getRegistry().register(blockReinforcedGlass = new BlockReinforcedGlass());
        //event.getRegistry().register(blockCombatRail = new BlockReinforcedRail());
        event.getRegistry().register(blockExplosive = new BlockExplosive());

        event.getRegistry().register(blockEmpTower = new BlockEmpTower());
        event.getRegistry().register(blockRadarStation = new BlockRadarStation());
        event.getRegistry().register(blockLaunchSupport = new BlockLaunchFrame());
        event.getRegistry().register(blockLaunchBase = new BlockLauncherBase());
        event.getRegistry().register(blockLaunchScreen = new BlockLaunchScreen());
        event.getRegistry().register(multiBlock = new BlockMultiblock());
        event.getRegistry().register(blockBattery = new BlockBattery());

        event.getRegistry().register(blockCruiseLauncher = new BlockCruiseLauncher());

        /*
        blockCamo = manager.newBlock("icbmCCamouflage", TileCamouflage.class);
        ICBMClassic.blockCruiseLauncher = ICBMClassic.INSTANCE.getManager().newBlock("icbmCCruiseLauncher", new TileCruiseLauncher());
        ICBMClassic.blockMissileCoordinator = ICBMClassic.INSTANCE.getManager().newBlock("icbmCMissileCoordinator", new TileMissileCoordinator());
        */

        GameRegistry.registerTileEntity(TileEntityExplosive.class, new ResourceLocation(ICBMClassic.DOMAIN, "explosive"));
        GameRegistry.registerTileEntity(TileEMPTower.class, new ResourceLocation(ICBMClassic.DOMAIN, "emptower"));
        GameRegistry.registerTileEntity(TileRadarStation.class, new ResourceLocation(ICBMClassic.DOMAIN, "radarstation"));
        GameRegistry.registerTileEntity(TileLauncherFrame.class, new ResourceLocation(ICBMClassic.DOMAIN, "launcherframe"));
        GameRegistry.registerTileEntity(TileLauncherBase.class, new ResourceLocation(ICBMClassic.DOMAIN, "launcherbase"));
        GameRegistry.registerTileEntity(TileLauncherScreen.class, new ResourceLocation(ICBMClassic.DOMAIN, "launcherscreen"));
        GameRegistry.registerTileEntity(TileMulti.class, new ResourceLocation(ICBMClassic.DOMAIN, "multiblock"));
        GameRegistry.registerTileEntity(TileEntityBattery.class, new ResourceLocation(ICBMClassic.DOMAIN, "batterybox"));
        GameRegistry.registerTileEntity(TileCruiseLauncher.class, new ResourceLocation(ICBMClassic.DOMAIN, "cruiseLauncher"));
    }
}
