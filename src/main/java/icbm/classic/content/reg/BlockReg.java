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
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry.ObjectHolder;

/**
 * Created by Dark(DarkGuardsman, Robert) on 1/7/19.
 */
@Mod.EventBusSubscriber(modid = ICBMClassic.DOMAIN)
public class BlockReg
{
    @ObjectHolder(ICBMClassic.PREFIX + "glassPressurePlate")
    public static Block blockGlassPlate;
    @ObjectHolder(ICBMClassic.PREFIX + "glassButton")
    public static Block blockGlassButton;
    @ObjectHolder(ICBMClassic.PREFIX + "spikes")
    public static Block blockSpikes;
    public static Block blockCamo; //TODO re-implement
    @ObjectHolder(ICBMClassic.PREFIX + "concrete")
    public static Block blockConcrete;
    @ObjectHolder(ICBMClassic.PREFIX + "reinforcedGlass")
    public static Block blockReinforcedGlass;
    @ObjectHolder(ICBMClassic.PREFIX + "explosives")
    public static Block blockExplosive;
    @ObjectHolder(ICBMClassic.PREFIX + "launcherbase")
    public static Block blockLaunchBase;
    @ObjectHolder(ICBMClassic.PREFIX + "launcherscreen")
    public static Block blockLaunchScreen;
    @ObjectHolder(ICBMClassic.PREFIX + "launcherframe")
    public static Block blockLaunchSupport;
    @ObjectHolder(ICBMClassic.PREFIX + "radarStation")
    public static Block blockRadarStation;
    @ObjectHolder(ICBMClassic.PREFIX + "emptower")
    public static Block blockEmpTower;
    @ObjectHolder(ICBMClassic.PREFIX + "cruiseLauncher")
    public static Block blockCruiseLauncher;
    public static Block blockMissileCoordinator; //TODO re-implement
    @ObjectHolder(ICBMClassic.PREFIX + "batterybox")
    public static Block blockBattery;
    @ObjectHolder(ICBMClassic.PREFIX + "multiblock")
    public static Block multiBlock;

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event)
    {
        event.getRegistry().register(new BlockGlassPressurePlate());
        event.getRegistry().register(new BlockGlassButton());
        event.getRegistry().register(new BlockSpikes());
        event.getRegistry().register(new BlockConcrete());
        event.getRegistry().register(new BlockReinforcedGlass());
        //event.getRegistry().register(blockCombatRail = new BlockReinforcedRail());
        event.getRegistry().register(new BlockExplosive());

        event.getRegistry().register(new BlockEmpTower());
        event.getRegistry().register(new BlockRadarStation());
        event.getRegistry().register(new BlockLaunchFrame());
        event.getRegistry().register(new BlockLauncherBase());
        event.getRegistry().register(new BlockLaunchScreen());
        event.getRegistry().register(new BlockMultiblock());
        event.getRegistry().register(new BlockBattery());

        event.getRegistry().register(new BlockCruiseLauncher());

        /*
        blockCamo = manager.newBlock("icbmCCamouflage", TileCamouflage.class);
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
