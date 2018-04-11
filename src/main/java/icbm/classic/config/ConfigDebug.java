package icbm.classic.config;

import icbm.classic.ICBMClassic;
import net.minecraftforge.common.config.Config;

/**
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 4/10/2018.
 */
@Config(modid = ICBMClassic.DOMAIN, name = "icbmclassic/debug")
@Config.LangKey("config.icbmclassic:debug.title")
public class ConfigDebug
{
    @Config.Name("debug_threads")
    @Config.Comment("Enabled extra console output to check the state of the explosive threads.")
    public static boolean DEBUG_THREADS = false;

    @Config.Name("debug_explosives")
    @Config.Comment("Enabled extra console output to check the state of the explosive logic.")
    public static boolean DEBUG_EXPLOSIVES = false;
}
