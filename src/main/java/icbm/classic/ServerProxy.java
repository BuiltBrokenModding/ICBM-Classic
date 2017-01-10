package icbm.classic;

import icbm.classic.content.machines.coordinator.TileMissileCoordinator;
import icbm.classic.content.machines.emptower.TileEMPTower;
import icbm.classic.content.machines.launcher.base.TileLauncherBase;
import icbm.classic.content.machines.launcher.cruise.TileCruiseLauncher;
import icbm.classic.content.machines.launcher.frame.TileLauncherFrame;
import icbm.classic.content.machines.launcher.screen.TileLauncherScreen;
import icbm.classic.content.machines.radarstation.TileRadarStation;

/**
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 1/9/2017.
 */
public class ServerProxy extends CommonProxy
{
    @Override
    public void preInit()
    {
        super.preInit();
        ICBMClassic.blockRadarStation = ICBMClassic.INSTANCE.getManager().newBlock("icbmCRadarStation", new TileRadarStation());
        ICBMClassic.blockEmpTower = ICBMClassic.INSTANCE.getManager().newBlock("icbmCEmpTower", new TileEMPTower());
        ICBMClassic.blockLaunchBase = ICBMClassic.INSTANCE.getManager().newBlock("icbmCLauncherBase", new TileLauncherBase());
        ICBMClassic.blockLaunchSupport = ICBMClassic.INSTANCE.getManager().newBlock("icbmCLauncherFrame", new TileLauncherFrame());
        ICBMClassic.blockLaunchScreen = ICBMClassic.INSTANCE.getManager().newBlock("icbmCLauncherScreen", new TileLauncherScreen());
        ICBMClassic.blockCruiseLauncher = ICBMClassic.INSTANCE.getManager().newBlock("icbmCCruiseLauncher", new TileCruiseLauncher());
        ICBMClassic.blockMissileCoordinator = ICBMClassic.INSTANCE.getManager().newBlock("icbmCMissileCoordinator", new TileMissileCoordinator());
    }
}
