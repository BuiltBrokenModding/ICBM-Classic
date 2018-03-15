package icbm.classic.config;

import icbm.classic.ICBMClassic;
import icbm.classic.content.entity.missile.EntityMissile;
import net.minecraftforge.common.config.Config;

/**
 * Configs for {@link EntityMissile}
 *
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 2/28/2018.
 */
@Config(modid = ICBMClassic.DOMAIN, name = "icbmclassic/missile")
@Config.LangKey("config.icbmclassic:blast.title")
public class ConfigMissile
{
    @Config.Name("speed")
    @Config.Comment("Speed limiter of the missile when moving upwards out of the launcher")
    @Config.RangeDouble(min = 0.0001, max = 10)
    public static float LAUNCH_SPEED = 0.012F;


    @Config.Name("nuclear_cluster_size")
    @Config.Comment("Number of missiles to spawn for nuclear cluster missile")
    @Config.RangeInt(min = 1, max = 30)
    public static int NUCLEAR_CLUSTER_SIZE = 4;

    @Config.Name("cluster_size")
    @Config.Comment("Number of missiles to spawn for cluster missile")
    @Config.RangeInt(min = 1, max = 30)
    public static int CLUSTER_SIZE = 12;
}
