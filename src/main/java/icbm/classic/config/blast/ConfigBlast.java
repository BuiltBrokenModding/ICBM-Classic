package icbm.classic.config.blast;

import icbm.classic.ICBMConstants;
import net.minecraftforge.common.config.Config;

/**
 *
 * Created by Dark(DarkGuardsman, Robert) on 2/22/2018.
 */
@Config(modid = ICBMConstants.DOMAIN, name = "icbmclassic/blast")
@Config.LangKey("config.icbmclassic:blast.title")
public class ConfigBlast
{
    @Config.Name("antimatter_size")
    @Config.Comment("Size of the antimatter blast")
    @Config.RangeInt(min = 1, max = 1000)
    public static int ANTIMATTER_SIZE = 55;

    @Config.Name("antimatter_break_unbreakable")
    @Config.Comment({"Should antimatter ignore hardness checks for unbreakable, allows destroying bedrock and warded stone.", "This config option does nothing if 'antimatter_break_blocks' is set to false."})
    public static boolean ANTIMATTER_DESTROY_UNBREAKABLE_BLOCKS = true;

    @Config.Name("antimatter_block_and_ent_dmg_on_redmatter")
    @Config.Comment("Whether or not antimatter damages blocks and entities when detonating and killing a black hole (caused by red matter explosives)")
    public static boolean ANTIMATTER_BLOCK_AND_ENT_DAMAGE_ON_REDMATTER = false;

    @Config.Name("blast_do_block_updates")
    @Config.Comment("Whether or not the big explosions trigger block updates for all blocks.\nSetting this to false leads to performance improvements, especially when dealing with a lot of water.")
    public static boolean BLAST_DO_BLOCKUPDATES = false;

    @Config.Name("exothermic_create_netherrack")
    @Config.Comment("Allows the exothermic to place netherrack in the world")
    public static boolean EXOTHERMIC_CREATE_NETHER_RACK = true;

    @Config.Name("allow_day_night_switch")
    @Config.Comment("Allows explosives to change time of day")
    public static boolean ALLOW_DAY_NIGHT = true;

    @Config.LangKey("config.icbmclassic:blast.redmatter.title")
    @Config.Comment("Set for redmatter blast")
    public static ConfigRedmatter REDMATTER = new ConfigRedmatter();

    @Config.LangKey("config.icbmclassic:blast.fusetimes.title")
    @Config.Comment("Set the time between ignition and explosion seperately for each bomb cart/explosive/grenade here.")
    public static ConfigFuseTimes FUSE_TIMES = new ConfigFuseTimes();

}
