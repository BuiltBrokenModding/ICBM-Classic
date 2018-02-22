package icbm.classic.config;

import icbm.classic.ICBMClassic;
import net.minecraftforge.common.config.Config;

/**
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 2/22/2018.
 */
@Config(modid = ICBMClassic.DOMAIN, name = "icbmclassic/blast")
@Config.LangKey("config.icbmclassic:blast.title")
public class ConfigBlast
{
    @Config.Name("antimatter_size")
    @Config.Comment("Size of the antimatter blast")
    @Config.RangeInt(min = 1, max = 1000)
    public static int ANTIMATTER_SIZE = 55;

    @Config.Name("antimatter_break_unbreakable")
    @Config.Comment("Should antimatter ignore hardness checks for unbreakable, allows destroying bedrock and warded stone")
    public static boolean ANTIMATTER_DESTROY_UNBREAKABLE_BLOCKS = true;

    @Config.Name("exothermic_create_netherrack")
    @Config.Comment("Allows the exothermic to place netherrack in the world")
    public static boolean EXOTHERMIC_CREATE_NETHER_RACK = true;
}
