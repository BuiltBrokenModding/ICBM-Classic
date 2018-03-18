package icbm.classic;

import com.builtbroken.mc.core.Engine;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
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

    /** Should camo block use glass as the render */
    public static boolean CAMO_USE_GLASS_RENDER = false;
    /** Block to use in place of the default vine render */
    public static String CAMO_BLOCK_TO_RENDER_AS_VINES = "minecraft:vines";
    public static Block _blockToUseForInsideIcon;


    public static void load(Configuration configuration)
    {
        //Launchers
        LAUNCHER_RANGE_TIER3 = configuration.getInt("tier_3_range", "launcher", Settings.LAUNCHER_RANGE_TIER3,
                0, Integer.MAX_VALUE, "Range of tier 3 launcher");
        LAUNCHER_RANGE_TIER2 = configuration.getInt("tier_2_range", "launcher", Settings.LAUNCHER_RANGE_TIER2,
                0, Integer.MAX_VALUE, "Range of tier 2 launcher");
        LAUNCHER_RANGE_TIER1 = configuration.getInt("tier_1_range", "launcher", Settings.LAUNCHER_RANGE_TIER1,
                0, Integer.MAX_VALUE, "Range of tier 1 launcher");

        //AntimatterANTIMATTER_SIZE
        ANTIMATTER_SIZE = configuration.getInt("blast_radius", "antimatter", ANTIMATTER_SIZE, 1, 1000,
                "Radius size of the antimatter blast, setting higher will result in more lag.");
        ANTIMATTER_DESTROY_UNBREAKABLE_BLOCKS = configuration.getBoolean("destroy_unbreakable_blocks", "antimatter",
                ANTIMATTER_DESTROY_UNBREAKABLE_BLOCKS, "Allows antimatter to destroy unbreakable blocks");

        //Hand held
        ROCKET_LAUNCHER_TIER_FIRE_LIMIT = configuration.getInt("tier_limit", "rocket_launcher",
                ROCKET_LAUNCHER_TIER_FIRE_LIMIT, 0, 4,
                "Limits the max missile tier for the hand held rocket launcher for non-creative mode users.");

        //Nightmare
        FORCE_ENABLE_NIGHTMARE = configuration.getBoolean("force_enable_nightmare_missile", "holiday",
                FORCE_ENABLE_NIGHTMARE, "Force enables the nightmare missile outside of halloween holiday");

        //Camo
        CAMO_USE_GLASS_RENDER = configuration.getBoolean("use_glass_render", "camo_block", CAMO_USE_GLASS_RENDER,
                "Allows changing the vine render to use glass instead for the see through part of camo blocks.");
        CAMO_BLOCK_TO_RENDER_AS_VINES = configuration.getString("vine_block", "camo_block", CAMO_BLOCK_TO_RENDER_AS_VINES,
                "");
    }

    public static Block getBlockToUseForInsideIcon()
    {
        if (_blockToUseForInsideIcon == null)
        {
            _blockToUseForInsideIcon = Block.getBlockFromName(CAMO_BLOCK_TO_RENDER_AS_VINES);
            if(_blockToUseForInsideIcon == null)
            {
                _blockToUseForInsideIcon = Blocks.vine;
            }
        }
        return _blockToUseForInsideIcon;
    }
}
