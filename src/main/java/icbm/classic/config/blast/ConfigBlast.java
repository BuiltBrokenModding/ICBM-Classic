package icbm.classic.config.blast;

import icbm.classic.config.blast.types.*;


/**
 *
 * Created by Dark(DarkGuardsman, Robin) on 2/22/2018.
 */
//@Config(modid = ICBMConstants.DOMAIN, name = "icbmclassic/blast")
//@Config.LangKey("config.icbmclassic:blast.title")
public class ConfigBlast
{
    //@Config.LangKey("config.icbmclassic:blast.condensed.title")
    //@Config.Comment("Settings for condensed explosive")
    public static ConfigCondensed condensed = new ConfigCondensed();

    //@Config.LangKey("config.icbmclassic:blast.shrapnel.title")
    //@Config.Comment("Settings for shrapnel explosive")
    public static ConfigShrapnel shrapnel = new ConfigShrapnel();

    //@Config.LangKey("config.icbmclassic:blast.incendiary.title")
    //@Config.Comment("Settings for incendiary explosive")
    public static ConfigExBasics incendiary = new ConfigExBasics(14);

    //@Config.LangKey("config.icbmclassic:blast.debilitation.title")
    //@Config.Comment("Settings for debilitation explosive")
    public static ConfigDebilitation debilitation = new ConfigDebilitation();

    //@Config.LangKey("config.icbmclassic:blast.chemical.title")
    //@Config.Comment("Settings for chemical explosive")
    public static ConfigChemical chemical = new ConfigChemical();

    //@Config.LangKey("config.icbmclassic:blast.anvil.title")
    //@Config.Comment("Settings for anvil explosive")
    public static ConfigAnvil anvil = new ConfigAnvil();

    //@Config.LangKey("config.icbmclassic:blast.repulsive.title")
    //@Config.Comment("Settings for repulsive explosive")
    public static ConfigExBasics repulsive = new ConfigExBasics(2);

    //@Config.LangKey("config.icbmclassic:blast.attractive.title")
    //@Config.Comment("Settings for attractive explosive")
    public static ConfigExBasics attractive = new ConfigExBasics(2);

    //@Config.LangKey("config.icbmclassic:blast.fragmentation.title")
    //@Config.Comment("Settings for fragmentation explosive")
    public static ConfigFragment fragmentation = new ConfigFragment();

    //@Config.LangKey("config.icbmclassic:blast.contagious.title")
    //@Config.Comment("Settings for contagious explosive")
    public static ConfigContagious contagious = new ConfigContagious();

    //@Config.LangKey("config.icbmclassic:blast.sonic.title")
    //@Config.Comment("Settings for sonic explosive")
    public static ConfigExBasics sonic = new ConfigExBasics(15);

    //@Config.LangKey("config.icbmclassic:blast.breaching.title")
    //@Config.Comment("Settings for breaching explosive")
    public static ConfigBreaching breaching = new ConfigBreaching();

    //@Config.LangKey("config.icbmclassic:blast.thermobaric.title")
    //@Config.Comment("Settings for thermobaric explosive")
    public static ConfigExBasics thermobaric = new ConfigExBasics(30);

    //@Config.LangKey("config.icbmclassic:blast.nuclear.title")
    //@Config.Comment("Settings for nuclear explosive")
    public static ConfigNuclear nuclear = new ConfigNuclear();

    @Deprecated
    //@Config.LangKey("config.icbmclassic:blast.emp.title")
    //@Config.Comment("Settings for emp explosive")
    public static ConfigExBasics emp = new ConfigExBasics(50);

    //@Config.LangKey("config.icbmclassic:blast.exothermic.title")
    //@Config.Comment("Settings for exothermic explosive")
    public static ConfigExBasics exothermic = new ConfigExBasics(30);

    //@Config.LangKey("config.icbmclassic:blast.endothermic.title")
    //@Config.Comment("Settings for endothermic explosive")
    public static ConfigExBasics endothermic = new ConfigExBasics(30);

    //@Config.LangKey("config.icbmclassic:blast.antigravitational.title")
    //@Config.Comment("Settings for antigravitational explosive")
    public static ConfigExBasics antigravitational = new ConfigExBasics(30);

    //@Config.LangKey("config.icbmclassic:blast.ender.title")
    //@Config.Comment("Settings for ender explosive")
    public static ConfigExBasics ender = new ConfigExBasics(30);

    //@Config.LangKey("config.icbmclassic:blast.antimatter.title")
    //@Config.Comment("Settings for antimatter explosive")
    public static ConfigAntimatter antimatter = new ConfigAntimatter();

    //@Config.LangKey("config.icbmclassic:blast.redmatter.title")
    //@Config.Comment("Set for redmatter blast")
    public static ConfigRedmatter redmatter = new ConfigRedmatter();

    //@Config.LangKey("config.icbmclassic:blast.colorful.title")
    //@Config.Comment("Set for colorful blast")
    public static ConfigExBasics colorful = new ConfigExBasics(10);

    //@Config.Name("blast_do_block_updates")
    //@Config.Comment("Whether or not the big explosions trigger block updates for all blocks.\nSetting this to false leads to performance improvements, especially when dealing with a lot of water.")
    public static boolean BLAST_DO_BLOCKUPDATES = false;

    @Deprecated //Move to sub-config
    //@Config.Name("allow_day_night_switch")
    //@Config.Comment("Allows explosives to change time of day")
    public static boolean ALLOW_DAY_NIGHT = true;

    @Deprecated //Move to each type
    //@Config.LangKey("config.icbmclassic:blast.fusetimes.title")
    //@Config.Comment("Set the time between ignition and explosion seperately for each bomb cart/explosive/grenade here.")
    public static ConfigFuseTimes FUSE_TIMES = new ConfigFuseTimes();

}
