package icbm.classic.config;

import icbm.classic.ICBMClassic;
import net.minecraftforge.common.config.Config;

/**
 * Settings class for various configuration settings.
 *
 * @author Calclavia, DarkCow
 */
@Config(modid = ICBMClassic.DOMAIN, name = "icbmclassic/launcher")
@Config.LangKey("config.icbmclassic:launcher.title")
public class ConfigLauncher
{
    /** Max range for the launcher tier 3 */
    @Config.Name("tier_3_range")
    @Config.Comment("Range of tier 3 launcher")
    public static int LAUNCHER_RANGE_TIER3 = 10000;
    /** Max range for the launcher tier 2 */
    @Config.Name("tier_2_range")
    @Config.Comment("Range of tier 2 launcher")
    public static int LAUNCHER_RANGE_TIER2 = 3000;
    /** Max range for the launcher tier 1 */
    @Config.Name("tier_1_range")
    @Config.Comment("Range of tier 1 launcher")
    public static int LAUNCHER_RANGE_TIER1 = 1000;
}
