package icbm.classic.config;

import icbm.classic.ICBMConstants;
import net.minecraftforge.common.config.Config;

/**
 * Configs for Missiles
 *
 * Created by Dark(DarkGuardsman, Robert) on 2/28/2018.
 */
@Config(modid = ICBMConstants.DOMAIN, name = "icbmclassic/missile")
@Config.LangKey("config.icbmclassic:missile.title")
public class ConfigMissile
{
    /**  */
    @Config.Name("speed_direct_fire")
    @Config.Comment("Speed (meters per tick) limiter of the missile when fired directly without a target")
    @Config.RangeDouble(min = 0.0001, max = 10)
    public static float DIRECT_FLIGHT_SPEED = 2;

    @Config.Name("speed_launch_pad")
    @Config.Comment("Speed (meters per tick) limiter of the missile when moving upwards out of the launcher")
    @Config.RangeDouble(min = 0.0001, max = 10)
    public static float LAUNCH_SPEED = 0.012F;

    @Config.Name("simulation_start_height")
    @Config.Comment("Height (y level) to start simulating a missile when it travels above the map")
    @Config.RangeInt(min = 1)
    public static int SIMULATION_START_HEIGHT = 300;

    @Config.Name("cruise_fuel")
    @Config.Comment("Fuel (ticks) before a missile fired from a cruise launcher starts to fall out of the air")
    @Config.RangeInt(min = 0)
    public static int CRUISE_FUEL = 200;

    @Config.Name("handheld_fuel")
    @Config.Comment("Fuel (ticks) before a missile fired from a handheld launcher starts to fall out of the air")
    @Config.RangeInt(min = 0)
    public static int HANDHELD_FUEL = 200;

    @Config.Name("health_tier_1")
    @Config.Comment("Amount of damage a missile can take from any source before death")
    @Config.RangeInt(min = 1)
    public static int TIER_1_HEALTH = 50;

    @Config.Name("health_tier_2")
    @Config.Comment("Amount of damage a missile can take from any source before death")
    @Config.RangeInt(min = 1)
    public static int TIER_2_HEALTH = 80;

    @Config.Name("health_tier_3")
    @Config.Comment("Amount of damage a missile can take from any source before death")
    @Config.RangeInt(min = 1)
    public static int TIER_3_HEALTH = 100;

    @Config.Name("health_tier_4")
    @Config.Comment("Amount of damage a missile can take from any source before death")
    @Config.RangeInt(min = 1)
    public static int TIER_4_HEALTH = 200;
}
