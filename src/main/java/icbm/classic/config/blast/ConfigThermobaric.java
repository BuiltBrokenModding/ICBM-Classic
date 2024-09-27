package icbm.classic.config.blast;

import net.minecraftforge.common.config.Config;

public class ConfigThermobaric {

    @Config.LangKey("config.icbmclassic:blast.scale.title")
    @Config.Comment("Scale of the explosive; This isn't always max size but a multiplier used in calculations.")
    @Config.RangeDouble(min = 1, max = Integer.MAX_VALUE)
    public double scale = 30;

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
}
