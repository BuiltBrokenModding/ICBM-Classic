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
    /** Max range for the launcher tier 3 */
    @Config.Name("max_range")
    @Config.Comment("Range of silo launcher in blocks (meters)")
    public static int RANGE = 10000;

    /** Size of the energy buffer for the launcher tier 3 */
    @Config.Name("tpower_capacity")
    @Config.Comment("Size of the energy buffer")
    public static int POWER_CAPACITY = 1200;

    /** Energy usage for the launcher tier 3 */
    @Config.Name("power_per_launch")
    @Config.Comment("Energy consumed per launcher")
    public static int POWER_COST = 1200;

    /** Energy usage for the launcher tier 3 */
    @Config.Name("launch_cooldown")
    @Config.Comment("Time in ticks (20 ticks a second) before launcher can fire again")
    public static int COOLDOWN = 20;
}
