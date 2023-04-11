package icbm.classic.config;

import icbm.classic.ICBMConstants;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Client only settings
 */
@Config(modid = ICBMConstants.DOMAIN, name = "icbmclassic/client")
@Config.LangKey("config.icbmclassic:client.title")
public class ConfigClient
{
    @Config.Name("missile_engine_smoke")
    @Config.Comment("Enables engine smoke effect for missiles")
    public static boolean MISSILE_ENGINE_SMOKE = true;

    @Config.Name("missile_launch_smoke")
    @Config.Comment("Enables smoke effect for launched missiles")
    public static boolean MISSILE_LAUNCH_SMOKE = true;
}
