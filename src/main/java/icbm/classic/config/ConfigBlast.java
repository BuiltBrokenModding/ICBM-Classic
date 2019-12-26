package icbm.classic.config;

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

    @Config.Name("redmatter_movement")
    @Config.Comment("Allows red matter explosions to be moved")
    public static boolean REDMATTER_MOVEMENT = true;

    @Config.Name("allow_day_night_switch")
    @Config.Comment("Allows explosives to change time of day")
    public static boolean ALLOW_DAY_NIGHT = true;

    @Config.LangKey("config.icbmclassic:blast.fusetimes.title")
    @Config.Comment("Set the time between ignition and explosion seperately for each bomb cart/explosive/grenade here.")
    public static FuseTimes FUSE_TIMES = new FuseTimes();

    public static class FuseTimes
    {
        @Config.LangKey("config.icbmclassic:blast.fusetimes.bombcarts.title")
        @Config.Comment("Set fuse times for bomb carts here. The times are written in ticks, where 20 ticks = 1 second.")
        public BombCarts BOMB_CARTS = new BombCarts();

        @Config.LangKey("config.icbmclassic:blast.fusetimes.explosives.title")
        @Config.Comment("Set fuse times for explosives (the block) here. The times are written in ticks, where 20 ticks = 1 second.")
        public Explosives EXPLOSIVES = new Explosives();

        @Config.LangKey("config.icbmclassic:blast.fusetimes.grenades.title")
        @Config.Comment("Set fuse times for grenades here. The times are written in ticks, where 20 ticks = 1 second.")
        public Grenades GRENADES = new Grenades();

        public class BombCarts
        {
            @Config.RangeInt(min = 0, max = Integer.MAX_VALUE)
            public int CONDENSED = 100;

            @Config.RangeInt(min = 0, max = Integer.MAX_VALUE)
            public int SHRAPNEL = 100;

            @Config.RangeInt(min = 0, max = Integer.MAX_VALUE)
            public int INCENDIARY = 100;

            @Config.RangeInt(min = 0, max = Integer.MAX_VALUE)
            public int DEBILITATION = 100;

            @Config.RangeInt(min = 0, max = Integer.MAX_VALUE)
            public int CHEMICAL = 100;

            @Config.RangeInt(min = 0, max = Integer.MAX_VALUE)
            public int ANVIL = 100;

            @Config.RangeInt(min = 0, max = Integer.MAX_VALUE)
            public int REPULSIVE = 120;

            @Config.RangeInt(min = 0, max = Integer.MAX_VALUE)
            public int ATTRACTIVE = 120;

            @Config.RangeInt(min = 0, max = Integer.MAX_VALUE)
            public int FRAGMENTATION = 100;

            @Config.RangeInt(min = 0, max = Integer.MAX_VALUE)
            public int CONTAGIOUS = 100;

            @Config.RangeInt(min = 0, max = Integer.MAX_VALUE)
            public int SONIC = 100;

            @Config.RangeInt(min = 0, max = Integer.MAX_VALUE)
            public int BREACHING = 0;

            @Config.RangeInt(min = 0, max = Integer.MAX_VALUE)
            public int THERMOBARIC = 100;

            @Config.RangeInt(min = 0, max = Integer.MAX_VALUE)
            public int NUCLEAR = 100;

            @Config.RangeInt(min = 0, max = Integer.MAX_VALUE)
            public int EMP = 100;

            @Config.RangeInt(min = 0, max = Integer.MAX_VALUE)
            public int EXOTHERMIC = 100;

            @Config.RangeInt(min = 0, max = Integer.MAX_VALUE)
            public int ENDOTHERMIC = 100;

            @Config.RangeInt(min = 0, max = Integer.MAX_VALUE)
            public int ANTI_GRAVITATIONAL = 100;

            @Config.RangeInt(min = 0, max = Integer.MAX_VALUE)
            public int ENDER = 100;

            @Config.RangeInt(min = 0, max = Integer.MAX_VALUE)
            public int HYPERSONIC = 100;

            @Config.RangeInt(min = 0, max = Integer.MAX_VALUE)
            public int ANTIMATTER = 300;

            @Config.RangeInt(min = 0, max = Integer.MAX_VALUE)
            public int REDMATTER = 100;
        }

        public class Explosives
        {
            @Config.RangeInt(min = 0, max = Integer.MAX_VALUE)
            public int CONDENSED = 0;

            @Config.RangeInt(min = 0, max = Integer.MAX_VALUE)
            public int SHRAPNEL = 100;

            @Config.RangeInt(min = 0, max = Integer.MAX_VALUE)
            public int INCENDIARY = 100;

            @Config.RangeInt(min = 0, max = Integer.MAX_VALUE)
            public int DEBILITATION = 100;

            @Config.RangeInt(min = 0, max = Integer.MAX_VALUE)
            public int CHEMICAL = 100;

            @Config.RangeInt(min = 0, max = Integer.MAX_VALUE)
            public int ANVIL = 100;

            @Config.RangeInt(min = 0, max = Integer.MAX_VALUE)
            public int REPULSIVE = 120;

            @Config.RangeInt(min = 0, max = Integer.MAX_VALUE)
            public int ATTRACTIVE = 120;

            @Config.RangeInt(min = 0, max = Integer.MAX_VALUE)
            public int FRAGMENTATION = 100;

            @Config.RangeInt(min = 0, max = Integer.MAX_VALUE)
            public int CONTAGIOUS = 100;

            @Config.RangeInt(min = 0, max = Integer.MAX_VALUE)
            public int SONIC = 100;

            @Config.RangeInt(min = 0, max = Integer.MAX_VALUE)
            public int BREACHING = 0;

            @Config.RangeInt(min = 0, max = Integer.MAX_VALUE)
            public int THERMOBARIC = 100;

            @Config.RangeInt(min = 0, max = Integer.MAX_VALUE)
            public int NUCLEAR = 100;

            @Config.RangeInt(min = 0, max = Integer.MAX_VALUE)
            public int EMP = 100;

            @Config.RangeInt(min = 0, max = Integer.MAX_VALUE)
            public int EXOTHERMIC = 100;

            @Config.RangeInt(min = 0, max = Integer.MAX_VALUE)
            public int ENDOTHERMIC = 100;

            @Config.RangeInt(min = 0, max = Integer.MAX_VALUE)
            public int ANTI_GRAVITATIONAL = 100;

            @Config.RangeInt(min = 0, max = Integer.MAX_VALUE)
            public int ENDER = 100;

            @Config.RangeInt(min = 0, max = Integer.MAX_VALUE)
            public int HYPERSONIC = 100;

            @Config.RangeInt(min = 0, max = Integer.MAX_VALUE)
            public int ANTIMATTER = 300;

            @Config.RangeInt(min = 0, max = Integer.MAX_VALUE)
            public int REDMATTER = 100;
        }

        public class Grenades
        {
            @Config.RangeInt(min = 0, max = Integer.MAX_VALUE)
            public int CONVENTIONAL = 100;

            @Config.RangeInt(min = 0, max = Integer.MAX_VALUE)
            public int SHRAPNEL = 100;

            @Config.RangeInt(min = 0, max = Integer.MAX_VALUE)
            public int INCENDIARY = 100;

            @Config.RangeInt(min = 0, max = Integer.MAX_VALUE)
            public int DEBILITATION = 100;

            @Config.RangeInt(min = 0, max = Integer.MAX_VALUE)
            public int CHEMICAL = 100;

            @Config.RangeInt(min = 0, max = Integer.MAX_VALUE)
            public int ANVIL = 100;

            @Config.RangeInt(min = 0, max = Integer.MAX_VALUE)
            public int REPULSIVE = 120;

            @Config.RangeInt(min = 0, max = Integer.MAX_VALUE)
            public int ATTRACTIVE = 120;
        }
    }
}
