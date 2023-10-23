package icbm.classic.config.blast.types;

import net.minecraftforge.common.config.Config;

public class ConfigNuclear {

    @Config.LangKey("config.icbmclassic:blast.scale.title")
    @Config.Comment("Scale of the explosive; This isn't always max size but a multiplier used in calculations.")
    @Config.RangeDouble(min = 1, max = Integer.MAX_VALUE)
    public double scale = 50;

    @Config.Comment("Scale of the rot blast")
    @Config.RangeDouble(min = 1, max = Integer.MAX_VALUE)
    public double rotScale = 80;

    @Config.Comment("Scale of the rot blast")
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

    @Config.Comment("Triggers the secondary rot blast")
    @Config.RangeDouble(min = 1, max = Integer.MAX_VALUE)
    public boolean useRotBlast = true;

    @Config.Comment("Triggers the secondary mutation blast")
    @Config.RangeDouble(min = 1, max = Integer.MAX_VALUE)
    public boolean useMutationBlast = true;
}
