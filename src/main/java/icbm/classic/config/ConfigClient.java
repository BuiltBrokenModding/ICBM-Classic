package icbm.classic.config;

import icbm.classic.IcbmConstants;
import net.neoforged.common.config.Config;

/**
 * Client only settings
 */
@Config(modid = IcbmConstants.MOD_ID, name = "icbmclassic/client")
@Config.LangKey("config.icbmclassic:client.title")
public class ConfigClient {
    @Config.Name("missile_engine_smoke")
    @Config.Comment("Enables engine smoke effect for missiles")
    public static boolean MISSILE_ENGINE_SMOKE = true;

    @Config.Name("missile_launch_smoke")
    @Config.Comment("Enables smoke effect for launched missiles")
    public static boolean MISSILE_LAUNCH_SMOKE = true;
}
