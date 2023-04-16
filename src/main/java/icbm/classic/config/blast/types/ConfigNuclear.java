package icbm.classic.config.blast.types;

import net.minecraftforge.common.config.Config;

public class ConfigNuclear {

    @Config.LangKey("config.icbmclassic:blast.scale.title")
    @Config.Comment("Scale of the explosive; This isn't always max size but a multiplier used in calculations.")
    @Config.RangeDouble(min = 1, max = Integer.MAX_VALUE)
    public double scale;

    @Config.LangKey("config.icbmclassic:blast.energy.title")
    @Config.Comment("Energy scale used for breaking blocks and doing entity damage")
    @Config.RangeDouble(min = 1, max = Integer.MAX_VALUE)
    public double energy;
    public ConfigNuclear(double scale, double energy) {
        this.scale = scale;
        this.energy = energy;
    }
}
