package icbm.classic.config;

import icbm.classic.ICBMConstants;
import net.minecraftforge.common.config.Config;

/**
 * Configs for Missiles
 *
 * Created by Dark(DarkGuardsman, Robert) on 2/28/2018.
 */
@Config(modid = ICBMConstants.DOMAIN, name = "icbmclassic/missile/anti")
@Config.LangKey("config.icbmclassic:missile.anti.title")
public class ConfigAntiMissile
{
    @Config.Name("speed")
    @Config.Comment("Speed (meters per tick) limiter of the missile")
    @Config.RangeDouble(min = 0.0001, max = 10)
    public static float FLIGHT_SPEED = 4;

    @Config.Name("fuel")
    @Config.Comment("Fuel (ticks) before a missile starts to fall out of the air")
    @Config.RangeInt(min = 0)
    public static int FUEL = 200;

    @Config.Name("attack_distance")
    @Config.Comment("Distance (meters) from target to trigger damage effect")
    @Config.RangeInt(min = 0)
    public static float ATTACK_DISTANCE = 0.5f;

    @Config.Name("attack_damage")
    @Config.Comment("Damage (hearts) to apply to the target when impacting")
    @Config.RangeInt(min = 0)
    public static float ATTACK_DAMAGE = 100;
}
