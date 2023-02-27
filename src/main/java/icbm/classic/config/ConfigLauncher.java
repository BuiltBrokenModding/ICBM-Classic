package icbm.classic.config;

import icbm.classic.ICBMConstants;
import net.minecraftforge.common.config.Config;

/**
 * Settings class for various configuration settings.
 *
 * @author Calclavia, DarkCow
 */
@Config(modid = ICBMConstants.DOMAIN, name = "icbmclassic/launcher")
@Config.LangKey("config.icbmclassic:launcher.title")
public class ConfigLauncher
{
    @Config.Name("max_range")
    @Config.Comment("Range of silo launcher in blocks (meters)")
    public static int RANGE = 10000;

    @Config.Name("min_inaccuracy_range")
    @Config.Comment("Inaccuracy offset to apply by default (meters)")
    public static double MIN_INACCURACY = 2;

    @Config.Name("scaled_inaccuracy_range")
    @Config.Comment("Inaccuracy offset to apply based on range (meters) with max range being full value. In addition to min inaccuracy already applied.")
    public static double SCALED_INACCURACY = 10;

    @Config.Name("scaled_inaccuracy_per_launcher")
    @Config.Comment("Inaccuracy offset to apply per launcher fired in the same circuit.")
    public static double SCALED_LAUNCHER_COST = 1f;

    @Config.Name("tpower_capacity")
    @Config.Comment("Size of the energy buffer")
    public static int POWER_CAPACITY = 1200;

    @Config.Name("power_per_launch")
    @Config.Comment("Energy consumed per launcher")
    public static int POWER_COST = 1200;

    @Config.Name("launch_cooldown")
    @Config.Comment("Time in ticks (20 ticks a second) before launcher can fire again")
    public static int COOLDOWN = 20;
}
