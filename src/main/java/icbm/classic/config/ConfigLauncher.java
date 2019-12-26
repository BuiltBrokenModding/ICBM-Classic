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
    @Config.Name("tier_3_range")
    @Config.Comment("Range of tier 3 launcher in blocks (meters)")
    public static int LAUNCHER_RANGE_TIER3 = 10000;
    /** Max range for the launcher tier 2 */
    @Config.Name("tier_2_range")
    @Config.Comment("Range of tier 2 launcher in blocks (meters)")
    public static int LAUNCHER_RANGE_TIER2 = 3000;
    /** Max range for the launcher tier 1 */
    @Config.Name("tier_1_range")
    @Config.Comment("Range of tier 1 launcher in blocks (meters)")
    public static int LAUNCHER_RANGE_TIER1 = 1000;

    /** Size of the energy buffer for the launcher tier 3 */
    @Config.Name("tier_3_power_capacity")
    @Config.Comment("Size of the energy buffer for the tier 3 launcher")
    public static int LAUNCHER_POWER_CAP_TIER3 = 36000;
    /** Size of the energy buffer for the launcher tier 2 */
    @Config.Name("tier_2_power_capacity")
    @Config.Comment("Size of the energy buffer for the tier 2 launcher")
    public static int LAUNCHER_POWER_CAP_TIER2 = 16000;
    /** Size of the energy buffer for the launcher tier 1 */
    @Config.Name("tier_1_power_capacity")
    @Config.Comment("Size of the energy buffer for the tier 1 launcher")
    public static int LAUNCHER_POWER_CAP_TIER1 = 5000;

    /** Energy usage for the launcher tier 3 */
    @Config.Name("tier_3_power_per_launch")
    @Config.Comment("Energy consumed per launch for the tier 3 launcher")
    public static int LAUNCHER_POWER_USAGE_TIER3 = 12000;
    /** Energy usage for the launcher tier 2 */
    @Config.Name("tier_2_power_per_launch")
    @Config.Comment("Energy consumed per launch for the tier 2 launcher")
    public static int LAUNCHER_POWER_USAGE_TIER2 = 8000;
    /** Energy usage for the launcher tier 1 */
    @Config.Name("tier_1_power_per_launch")
    @Config.Comment("Energy consumed per launch for the tier 1 launcher")
    public static int LAUNCHER_POWER_USAGE_TIER1 = 4000;

    /** Energy usage for the launcher tier 3 */
    @Config.Name("tier_3_delay")
    @Config.Comment("Time in ticks (20 ticks a second) to delay missile launching for the tier 3 launcher")
    public static int LAUNCHER_DELAY_TIER3 = 60;
    /** Energy usage for the launcher tier 2 */
    @Config.Name("tier_2_delay")
    @Config.Comment("Time in ticks (20 ticks a second) to delay missile launching for the tier 2 launcher")
    public static int LAUNCHER_DELAY_TIER2 = 80;
    /** Energy usage for the launcher tier 1 */
    @Config.Name("tier_1_delay")
    @Config.Comment("Time in ticks (20 ticks a second) to delay missile launching for the tier 1 launcher")
    public static int LAUNCHER_DELAY_TIER1 = 100;


    @Config.Name("tier_3_redstone")
    @Config.Comment("true if missiles can be launched with the tier 3 launcher using redstone, false to disable")
    public static boolean LAUNCHER_REDSTONE_TIER3 = true;
    @Config.Name("tier_2_redstone")
    @Config.Comment("true if missiles can be launched with the tier 2 launcher using redstone, false to disable")
    public static boolean LAUNCHER_REDSTONE_TIER2 = true;
    @Config.Name("tier_1_redstone")
    @Config.Comment("true if missiles can be launched with the tier 1 launcher using redstone, false to disable")
    public static boolean LAUNCHER_REDSTONE_TIER1 = true;
}
