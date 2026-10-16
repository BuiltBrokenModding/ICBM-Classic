package icbm.classic.config.missile;

import icbm.classic.lib.config.Config;

/**
 * Config for Surface to Air missiles
 */
public class ConfigSAMMissile
{
    @Config.Name("speed")
    @Config.Comment("Speed (meters per tick) limiter of the missile")
    @Config.RangeDouble(min = 0.0001, max = 10)
    public float FLIGHT_SPEED = 4;

    @Config.Name("use_radar_only")
    @Config.Comment("Forces seeker to only use radar data for better performance. See wiki for supported radar entities. Default is AABB scanning using world entity data.")
    public boolean RADAR_MAP_ONLY = false;

    @Config.Name("target_range")
    @Config.Comment("Range (meters) to limit scanning for new targets. Higher values will cause lag if using AABB scanning. Set use_radar_only=true to disable AABB scanning.")
    @Config.RangeInt(min = 1)
    public int TARGET_RANGE = 30;

    @Config.Name("fuel")
    @Config.Comment("Fuel (ticks) before a missile starts to fall out of the air")
    @Config.RangeInt(min = 0)
    public int FUEL = 200;

    @Config.Name("attack_damage")
    @Config.Comment("Damage (hearts) to apply to the target on intercept")
    @Config.RangeDouble(min = 0)
    public float ATTACK_DAMAGE = 100;
}
