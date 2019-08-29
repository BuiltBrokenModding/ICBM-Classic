package icbm.classic.config;

import icbm.classic.ICBMClassic;
import net.minecraftforge.common.config.Config;

/**
 *
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

    @Config.Name("blast_do_block_updates")
    @Config.Comment("Whether or not the big explosions trigger block updates for all blocks.\nSetting this to false leads to performance improvements, especially when dealing with a lot of water.")
    public static boolean BLAST_DO_BLOCKUPDATES = false;

    @Config.Name("exothermic_create_netherrack")
    @Config.Comment("Allows the exothermic to place netherrack in the world")
    public static boolean EXOTHERMIC_CREATE_NETHER_RACK = true;

    @Config.Name("redmatter_movement")
    @Config.Comment("Allows red matter explosions to be moved")
    public static boolean REDMATTER_MOVEMENT = true;

    @Config.Name("allow_day_night_switch")
    @Config.Comment("Allows explosives to change time of day")
    public static boolean ALLOW_DAY_NIGHT = true;
}
