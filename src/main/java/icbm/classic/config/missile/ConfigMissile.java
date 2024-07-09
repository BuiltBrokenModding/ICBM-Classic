package icbm.classic.config.missile;

import icbm.classic.ICBMConstants;
import net.minecraftforge.common.config.Config;

/**
 * Configs for Missiles
 *
 * Created by Dark(DarkGuardsman, Robin) on 2/28/2018.
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

    @Config.Name("simulation_flight_speed")
    @Config.Comment("Speed (meters per tick) of missiles in simulation")
    @Config.RangeDouble(min = 0.0001)
    public static float SIMULATION_FLIGHT_SPEED = 10;

    @Config.Name("simulation_exit_height")
    @Config.Comment("Height (y level) to start simulating a missile when it travels above the map")
    @Config.RangeInt(min = 1)
    public static int SIMULATION_EXIT_HEIGHT = 500;

    @Config.Name("simulation_enter_height")
    @Config.Comment("Height (y level) to spawn missiles when they enter the map")
    @Config.RangeInt(min = 1)
    public static int SIMULATION_ENTER_HEIGHT = 450;

    @Config.Name("arc_distance_limit")
    @Config.Comment("Distance, ignoring y, to allow flying in an arc towards target. Over distance will fly strait up for better performance.")
    @Config.RangeInt(min = 0)
    public static int ARC_DISTANCE_LIMIT = 500;

    @Config.Name("simulation_enter_speed")
    @Config.Comment("Speed (meters per tick) to spawn the missile")
    @Config.RangeDouble(min = 0)
    public static float SIMULATION_ENTER_SPEED = 1;

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

    @Config.Name("impact_damage_limit")
    @Config.Comment("Max amount of damage a missile can apply to a target on impact. Set to -1 to have no limit. Set to 0 to disable")
    @Config.RangeInt(min = -1)
    public static int DAMAGE_LIMIT = -1;

    @Config.Name("impact_damage_scale")
    @Config.Comment("scale * velocity = damage. RPG fired missiles often impact with a velocity of 2")
    @Config.RangeDouble(min = 0)
    public static float DAMAGE_SCALE = 20f;

    @Config.LangKey("config.icbmclassic:missile.sam.title")
    public static ConfigSAMMissile SAM_MISSILE = new ConfigSAMMissile();

    @Config.LangKey("config.icbmclassic:bomblet.title")
    public static ConfigBomblet bomblet = new ConfigBomblet();

    @Config.Name("cluster")
    @Config.LangKey("config.icbmclassic:missile.cluster.title")
    @Config.Comment("https://github.com/BuiltBrokenModding/ICBM-Classic/wiki/config-cluster")
    public static ConfigClusterMissile CLUSTER_MISSILE = new ConfigClusterMissile();

    @Config.Name("cargo_holder")
    @Config.LangKey("config.icbmclassic:cargo.holder.title")
    @Config.Comment("https://github.com/BuiltBrokenModding/ICBM-Classic/wiki/Config-Cargo-Holders")
    public static ConfigCargoItem CARGO_HOLDERS = new ConfigCargoItem();
}
