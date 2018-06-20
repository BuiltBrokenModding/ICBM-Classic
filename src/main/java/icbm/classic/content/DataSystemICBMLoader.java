package icbm.classic.content;

import com.builtbroken.mc.framework.computer.DataSystemHandler;
import com.builtbroken.mc.framework.mod.loadable.AbstractLoadable;
import icbm.classic.ICBMClassic;
import icbm.classic.content.machines.emptower.DSEmpTower;
import icbm.classic.content.machines.launcher.cruise.TileCruiseLauncher;
import icbm.classic.content.machines.launcher.screen.TileLauncherScreen;
import icbm.classic.content.machines.radarstation.TileRadarStation;
import icbm.classic.prefab.TileICBMMachine;

/**
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 6/20/2018.
 */
public class DataSystemICBMLoader extends AbstractLoadable
{
    @Override
    public void init()
    {
        DataSystemHandler.addSharedMethod("getEnergyUsage", tile -> {
            if (tile instanceof TileICBMMachine)
            {
                return (host, method, args) -> {
                    if (args == null || args.length == 0)
                    {
                        if (host instanceof TileICBMMachine)
                        {
                            return ((TileICBMMachine) host).getEnergyConsumption();
                        }
                        return "Error: tile is not an ICBM machine";
                    }
                    return "Error: getEnergyUsage() requires no arguments";
                };
            }
            return null;
        });

        DSEmpTower.load();
        DataSystemHandler.generate(ICBMClassic.PREFIX + "launcherScreen", TileLauncherScreen.class);
        DataSystemHandler.generate(ICBMClassic.PREFIX + "cruiseLauncher", TileCruiseLauncher.class);
        DataSystemHandler.generate(ICBMClassic.PREFIX + "radarStation", TileRadarStation.class);
    }
}
