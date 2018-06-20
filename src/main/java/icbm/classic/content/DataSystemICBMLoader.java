package icbm.classic.content;

import com.builtbroken.mc.framework.computer.DataSystemHandler;
import com.builtbroken.mc.framework.mod.loadable.AbstractLoadable;
import com.builtbroken.mc.imp.transform.vector.Pos;
import icbm.classic.ICBMClassic;
import icbm.classic.content.explosive.Explosives;
import icbm.classic.content.machines.emptower.DSEmpTower;
import icbm.classic.content.machines.launcher.TileLauncherPrefab;
import icbm.classic.content.machines.launcher.base.TileLauncherBase;
import icbm.classic.content.machines.launcher.cruise.TileCruiseLauncher;
import icbm.classic.content.machines.launcher.screen.TileLauncherScreen;
import icbm.classic.content.machines.radarstation.TileRadarStation;
import icbm.classic.prefab.TileFrequency;
import icbm.classic.prefab.TileICBMMachine;
import resonant.api.explosion.ILauncherController;

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

        missileSystem();
        frequencySystem();

        DSEmpTower.load();
        DataSystemHandler.createNewDataSystem("icbm.launcher.screen", new TileLauncherScreen());
        DataSystemHandler.createNewDataSystem("icbm.launcher.cruise", new TileCruiseLauncher());
        DataSystemHandler.createRedirectTile(new TileLauncherBase(), tile -> {
            if (tile instanceof TileLauncherBase)
            {
                return ((TileLauncherBase) tile).launchScreen;
            }
            return null;
        });
        DataSystemHandler.generate(ICBMClassic.PREFIX + "radarStation", TileRadarStation.class); //TODO convert to new system
    }

    private void frequencySystem()
    {
        DataSystemHandler.addSharedMethod("getFrequency", tile ->
        {
            if (tile instanceof TileFrequency)
            {
                return (host, method, args) -> {
                    if (args == null || args.length == 0)
                    {
                        if (host instanceof TileFrequency)
                        {
                            return ((TileFrequency) host).getFrequency();
                        }
                        return "Error: tile is not an ICBM frequency tile";
                    }
                    return "Error: getFrequency() requires no arguments";
                };
            }
            return null;
        });

        DataSystemHandler.addSharedMethod("setFrequency", tile ->
        {
            if (tile instanceof TileFrequency)
            {
                return (host, method, args) -> {
                    if (args != null && args.length == 1)
                    {
                        if (host instanceof TileFrequency)
                        {
                            return ((TileFrequency) host).setFrequency((int) getDouble(args[0]));
                        }
                        return "Error: tile is not an ICBM frequency tile";
                    }
                    return "Error: getFrequency(frequency) expects 1 argument";
                };
            }
            return null;
        });
    }

    private void missileSystem()
    {
        DataSystemHandler.addSharedMethod("getMissileTypeName", tile ->
        {
            if (tile instanceof TileLauncherPrefab)
            {
                return (host, method, args) -> {
                    if (args == null || args.length == 0)
                    {
                        if (host instanceof TileLauncherPrefab)
                        {
                            return ((TileLauncherPrefab) host).getMissileTypeName();
                        }
                        return "Error: tile is not an ICBM launcher";
                    }
                    return "Error: getMissileTypeName() requires no arguments";
                };
            }
            return null;
        });

        DataSystemHandler.addSharedMethod("getMissileType", tile ->
        {
            if (tile instanceof TileLauncherPrefab)
            {
                return (host, method, args) -> {
                    if (args == null || args.length == 0)
                    {
                        if (host instanceof TileLauncherPrefab)
                        {
                            Explosives ex = ((TileLauncherPrefab) host).getMissileType();
                            if (ex != null)
                            {
                                return ex.ordinal();
                            }
                            return -1;
                        }
                        return "Error: tile is not an ICBM launcher";
                    }
                    return "Error: getMissileType() requires no arguments";
                };
            }
            return null;
        });

        DataSystemHandler.addSharedMethod("getMissileTarget", tile ->
        {
            if (tile instanceof ILauncherController)
            {
                return (host, method, args) -> {
                    if (args == null || args.length == 0)
                    {
                        if (host instanceof ILauncherController)
                        {
                            Pos target = ((ILauncherController) host).getTarget();
                            return new Object[]{target.x(), target.y(), target.z()};
                        }
                        return "Error: tile is not an ICBM launcher";
                    }
                    return "Error: getMissileTarget() requires no arguments";
                };
            }
            return null;
        });

        DataSystemHandler.addSharedMethod("setMissileTarget", tile ->
        {
            if (tile instanceof ILauncherController)
            {
                return (host, method, args) -> {
                    if (args != null && args.length == 3)
                    {
                        if (host instanceof ILauncherController)
                        {
                            ((ILauncherController) host).setTarget(getDouble(args[0]), getDouble(args[1]), getDouble(args[2]));
                            return null;
                        }
                        return "Error: tile is not an ICBM launcher";
                    }
                    return "Error: setMissileTarget(x, y, z) expects 3 arguments for position";
                };
            }
            return null;
        });

        DataSystemHandler.addSharedMethod("canFireMissile", tile ->
        {
            if (tile instanceof ILauncherController)
            {
                return (host, method, args) -> {
                    if (args == null || args.length == 0)
                    {
                        if (host instanceof ILauncherController)
                        {
                            return ((ILauncherController) host).canLaunch();
                        }
                        return "Error: tile is not an ICBM launcher controller";
                    }
                    return "Error: canFireMissile() requires no arguments";
                };
            }
            return null;
        });

        DataSystemHandler.addSharedMethod("fireMissile", tile ->
        {
            if (tile instanceof ILauncherController)
            {
                return (host, method, args) -> {
                    if (args == null || args.length == 0)
                    {
                        if (host instanceof ILauncherController)
                        {
                            return ((ILauncherController) host).launch();
                        }
                        return "Error: tile is not an ICBM launcher controller";
                    }
                    return "Error: fireMissile() requires no arguments";
                };
            }
            return null;
        });
    }

    private double getDouble(Object object)
    {
        if (object instanceof Number)
        {
            return ((Number) object).doubleValue();
        }
        else if (object instanceof String)
        {
            return Double.parseDouble(((String) object).trim());
        }

        throw new NumberFormatException();
    }

}
