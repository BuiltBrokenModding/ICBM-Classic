package icbm.classic;

import com.builtbroken.mc.core.Engine;
import net.minecraftforge.common.config.Configuration;

/**
 * Settings class for various configuration settings.
 *
 * @author Calclavia, DarkCow
 */
public class Settings
{
    /** Max range for the launcher tier 3 */
    public static int LAUNCHER_RANGE_TIER3 = 10000;
    /** Max range for the launcher tier 2 */
    public static int LAUNCHER_RANGE_TIER2 = 3000;
    /** Max range for the launcher tier 1 */
    public static int LAUNCHER_RANGE_TIER1 = 1000;

    public static int ANTIMATTER_SIZE = 55;
    public static boolean ANTIMATTER_DESTROY_UNBREAKABLE_BLOCKS = true;
    public static int ROCKET_LAUNCHER_TIER_FIRE_LIMIT = 2;

    public static boolean FORCE_ENABLE_NIGHTMARE = Engine.runningAsDev;


    public static void load(Configuration configuration)
    {
        //Launchers
        LAUNCHER_RANGE_TIER3 = configuration.getInt( "tier_3_range","launcher", Settings.LAUNCHER_RANGE_TIER3,
                0, Integer.MAX_VALUE, "Range of tier 3 launcher");
        LAUNCHER_RANGE_TIER2 = configuration.getInt("tier_2_range","launcher",  Settings.LAUNCHER_RANGE_TIER2,
                0, Integer.MAX_VALUE, "Range of tier 2 launcher");
        LAUNCHER_RANGE_TIER1 = configuration.getInt("tier_1_range","launcher",  Settings.LAUNCHER_RANGE_TIER1,
                0, Integer.MAX_VALUE, "Range of tier 1 launcher");

        //AntimatterANTIMATTER_SIZE
        ANTIMATTER_SIZE = configuration.getInt("blast_radius","antimatter",  ANTIMATTER_SIZE, 1, 1000,
                "Radius size of the antimatter blast, setting higher will result in more lag.");
        ANTIMATTER_DESTROY_UNBREAKABLE_BLOCKS = configuration.getBoolean( "destroy_unbreakable_blocks","antimatter",
                ANTIMATTER_DESTROY_UNBREAKABLE_BLOCKS, "Allows antimatter to destroy unbreakable blocks");

        //Hand held
        ROCKET_LAUNCHER_TIER_FIRE_LIMIT = configuration.getInt("tier_limit", "rocket_launcher",
                ROCKET_LAUNCHER_TIER_FIRE_LIMIT, 0, 4,
                "Limits the max missile tier for the hand held rocket launcher for non-creative mode users.");

        //Nightmare
        FORCE_ENABLE_NIGHTMARE = configuration.getBoolean("force_enable_nightmare_missile","holiday",
                false, "Force enables the nightmare missile outside of halloween holiday");
    }
}
