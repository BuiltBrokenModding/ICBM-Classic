package icbm.classic.config;

import icbm.classic.IcbmConstants;
import icbm.classic.world.entity.flyingblock.FlyingBlock;
import net.neoforged.common.config.Config;
import net.neoforged.common.config.ConfigManager;
import net.neoforged.fml.client.event.ConfigChangedEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.common.eventhandler.SubscribeEvent;

/**
 * Settings class for various configuration settings.
 *
 * @author Calclavia, DarkCow
 */
@Config(modid = IcbmConstants.MOD_ID, name = "icbmclassic/main")
@Config.LangKey("config.icbmclassic:main.title")
@Mod.EventBusSubscriber(modid = IcbmConstants.MOD_ID)
public class ConfigMain {
    @Config.Name("use_energy")
    @Config.Comment("Range of tier 1 launcher")
    public static boolean REQUIRES_POWER = true;

    @Config.Name("handheld_launcher_tier_limit")
    @Config.Comment("Limits the max tier the handheld launcher can fire,} outside of creative mode")
    @Config.RangeInt(min = 1, max = 4)
    public static int ROCKET_LAUNCHER_TIER_FIRE_LIMIT = 2;

    @SubscribeEvent
    public static void onConfigChangedEvent(final ConfigChangedEvent.OnConfigChangedEvent event) {
        if (event.getModID().equals(IcbmConstants.MOD_ID)) {
            ConfigManager.sync(IcbmConstants.MOD_ID, Config.Type.INSTANCE);

            // Reload config so we can convert to easier to hash lists
            FlyingBlock.loadFromConfig();
        }
    }
}
