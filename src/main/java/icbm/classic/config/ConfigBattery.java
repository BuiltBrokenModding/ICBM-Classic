package icbm.classic.config;

import icbm.classic.ICBMConstants;
import net.minecraftforge.common.config.Config;

/**
 *
 * Created by Dark(DarkGuardsman, Robert) on 2/22/2018.
 */
@Config(modid = ICBMConstants.DOMAIN, name = "icbmclassic/battery")
@Config.LangKey("config.icbmclassic:battery.title")
public class ConfigBattery
{
    @Config.Name("battery_tier_1_capacity")
    @Config.Comment("Amount of energy the battery can store")
    @Config.RangeInt(min = 1)
    public static int BATTERY_CAPACITY = 100000;

    @Config.Name("battery_tier_1_input")
    @Config.Comment("Transfer limit into the battery")
    @Config.RangeInt(min = 1)
    public static int BATTERY_INPUT_LIMIT = 10000;

    @Config.Name("battery_tier_1_output")
    @Config.Comment("Transfer limit out of the battery")
    @Config.RangeInt(min = 1)
    public static int BATTERY_OUTPUT_LIMIT = 10000;
}
