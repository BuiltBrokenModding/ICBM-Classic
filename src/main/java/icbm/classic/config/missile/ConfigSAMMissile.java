package icbm.classic.config.missile;

import net.neoforged.common.config.Config;

/**
 * Config for Surface to Air missiles
 */
public class ConfigSAMMissile {
    @Config.Name("speed")
    @Config.Comment("Speed (meters per tick) limiter of the missile")
    @Config.RangeDouble(min = 0.0001, max = 10)
    public float FLIGHT_SPEED = 4;

    @Config.Name("fuel")
    @Config.Comment("Fuel (ticks) before a missile starts to fall out of the air")
    @Config.RangeInt(min = 0)
    public int FUEL = 200;

    @Config.Name("attack_damage")
    @Config.Comment("Damage (hearts) to apply to the target when impacting")
    @Config.RangeInt(min = 0)
    public float ATTACK_DAMAGE = 100;
}
