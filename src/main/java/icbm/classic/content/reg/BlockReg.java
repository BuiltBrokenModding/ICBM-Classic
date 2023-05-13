package icbm.classic.content.reg;

import icbm.classic.ICBMConstants;
import icbm.classic.content.blocks.*;
import icbm.classic.content.blocks.emptower.BlockEmpTower;
import icbm.classic.content.blocks.emptower.TileEMPTower;
import icbm.classic.content.blocks.emptower.TileEmpTowerFake;
import icbm.classic.content.blocks.explosive.BlockExplosive;
import icbm.classic.content.blocks.explosive.TileEntityExplosive;
import icbm.classic.content.blocks.launcher.base.BlockLauncherBase;
import icbm.classic.content.blocks.launcher.base.TileLauncherBase;
import icbm.classic.content.blocks.launcher.cruise.BlockCruiseLauncher;
import icbm.classic.content.blocks.launcher.cruise.TileCruiseLauncher;
import icbm.classic.content.blocks.launcher.connector.BlockLaunchConnector;
import icbm.classic.content.blocks.launcher.connector.TileLauncherConnector;
import icbm.classic.content.blocks.launcher.frame.BlockLaunchFrame;
import icbm.classic.content.blocks.launcher.frame.TileLauncherFrame;
import icbm.classic.content.blocks.launcher.screen.BlockLaunchScreen;
import icbm.classic.content.blocks.launcher.screen.TileLauncherScreen;
import icbm.classic.content.blocks.multiblock.BlockMultiblock;
import icbm.classic.content.blocks.multiblock.TileMulti;
import icbm.classic.content.blocks.radarstation.BlockRadarStation;
import icbm.classic.content.blocks.radarstation.TileRadarStation;
import net.minecraft.block.Block;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry.ObjectHolder;

/**
 * Created by Dark(DarkGuardsman, Robert) on 1/7/19.
 */
@Mod.EventBusSubscriber(modid = ICBMConstants.DOMAIN)
public class BlockReg
{
    @ObjectHolder(ICBMConstants.PREFIX + "glassPressurePlate")
    public static Block blockGlassPlate;
    @ObjectHolder(ICBMConstants.PREFIX + "glassButton")
    public static Block blockGlassButton;
    @ObjectHolder(ICBMConstants.PREFIX + "spikes")
    public static Block blockSpikes;
    @ObjectHolder(ICBMConstants.PREFIX + "concrete")
    public static Block blockConcrete;
    @ObjectHolder(ICBMConstants.PREFIX + "reinforcedGlass")
    public static Block blockReinforcedGlass;
    @ObjectHolder(ICBMConstants.PREFIX + "explosives")
    public static Block blockExplosive;
    @ObjectHolder(ICBMConstants.PREFIX + "launcherbase")
    public static Block blockLaunchBase;
    @ObjectHolder(ICBMConstants.PREFIX + "launcherscreen")
    public static Block blockLaunchScreen;
    @ObjectHolder(ICBMConstants.PREFIX + "launcherframe")
    public static Block blockLaunchSupport;
    @ObjectHolder(ICBMConstants.PREFIX + "launcher_connector")
    public static Block blockLaunchConnector;
    @ObjectHolder(ICBMConstants.PREFIX + "radarStation")
    public static Block blockRadarStation;
    @ObjectHolder(ICBMConstants.PREFIX + "emptower")
    public static Block blockEmpTower;
    @ObjectHolder(ICBMConstants.PREFIX + "cruiseLauncher")
    public static Block blockCruiseLauncher;
    @ObjectHolder(ICBMConstants.PREFIX + "multiblock")
    public static Block multiBlock;

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event)
    {
        event.getRegistry().register(new BlockGlassPressurePlate());
        event.getRegistry().register(new BlockGlassButton());
        event.getRegistry().register(new BlockSpikes());
        event.getRegistry().register(new BlockConcrete());
        event.getRegistry().register(new BlockReinforcedGlass());
        event.getRegistry().register(new BlockExplosive());

        event.getRegistry().register(new BlockEmpTower());
        event.getRegistry().register(new BlockRadarStation());
        event.getRegistry().register(new BlockLaunchFrame());
        event.getRegistry().register(new BlockLaunchConnector());
        event.getRegistry().register(new BlockLauncherBase());
        event.getRegistry().register(new BlockLaunchScreen());
        event.getRegistry().register(new BlockMultiblock());

        event.getRegistry().register(new BlockCruiseLauncher());

        GameRegistry.registerTileEntity(TileEntityExplosive.class, new ResourceLocation(ICBMConstants.DOMAIN, "explosive"));
        TileEMPTower.register();
        GameRegistry.registerTileEntity(TileEmpTowerFake.class, new ResourceLocation(ICBMConstants.DOMAIN, "emptower_fake"));
        GameRegistry.registerTileEntity(TileRadarStation.class, new ResourceLocation(ICBMConstants.DOMAIN, "radarstation"));
        GameRegistry.registerTileEntity(TileLauncherFrame.class, new ResourceLocation(ICBMConstants.DOMAIN, "launcherframe"));
        GameRegistry.registerTileEntity(TileLauncherConnector.class, new ResourceLocation(ICBMConstants.DOMAIN, "launcher_connector"));
        GameRegistry.registerTileEntity(TileLauncherBase.class, new ResourceLocation(ICBMConstants.DOMAIN, "launcherbase"));
        GameRegistry.registerTileEntity(TileLauncherScreen.class, new ResourceLocation(ICBMConstants.DOMAIN, "launcherscreen"));
        GameRegistry.registerTileEntity(TileMulti.class, new ResourceLocation(ICBMConstants.DOMAIN, "multiblock"));
        GameRegistry.registerTileEntity(TileCruiseLauncher.class, new ResourceLocation(ICBMConstants.DOMAIN, "cruiseLauncher"));
    }
}
