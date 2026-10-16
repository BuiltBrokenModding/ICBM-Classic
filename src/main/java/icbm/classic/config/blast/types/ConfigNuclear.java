package icbm.classic.config.blast.types;

import icbm.classic.lib.config.Config;

public class ConfigNuclear {

    @Config.LangKey("config.icbmclassic:blast.scale.title")
    @Config.Comment("Scale of the explosive; This isn't always max size but a multiplier used in calculations.")
    @Config.RangeDouble(min = 1, max = Integer.MAX_VALUE)
    public double scale = 50;

    @Config.Comment("Scale of the block rot blast. Handles radioactive blocks and destroying plants")
    @Config.RangeDouble(min = 1, max = Integer.MAX_VALUE)
    public double rotScale = 80;

    @Config.Comment("Scale of the entity mutation blast")
    @Config.RangeDouble(min = 1, max = Integer.MAX_VALUE)
    public double mutationScale = 80;

    @Config.Comment("Scale of the entity damage")
    @Config.RangeDouble(min = 1, max = Integer.MAX_VALUE)
    public double entityDamageScale = 50;

    @Config.LangKey("config.icbmclassic:blast.energy.title")
    @Config.Comment("Energy scale used for breaking blocks and doing entity damage")
    @Config.RangeDouble(min = 1, max = Integer.MAX_VALUE)
    public double energy = 80;

    @Config.Comment("Multiplier to apply to energy before scaling blast damage to entities. Damage still scales by distance and other factors.")
    @Config.RangeDouble(min = 1, max = Integer.MAX_VALUE)
    public double entityDamageMultiplier = 1000;

    @Config.Name("radioactive_replacements")
    public BlockReplacements radiationReplacements = new BlockReplacements();

    public static class BlockReplacements {

        @Config.Name("list")
        @Config.Comment("Sets which block to use when replacing a block during a radiation event Docs: https://github.com/BuiltBrokenModding/ICBM-Classic/wiki/config-block-states")
        public String[] blockStates = new String[]{};
    }
}
