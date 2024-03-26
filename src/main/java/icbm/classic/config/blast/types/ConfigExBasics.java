package icbm.classic.config.blast.types;

import net.neoforged.neoforge.common.config.Config;

public class ConfigExBasics {

    @Config.LangKey("config.icbmclassic:blast.scale.title")
    @Config.Comment("Scale of the explosive; This isn't always max size but a multiplier used in calculations.")
    @Config.RangeDouble(min = 1, max = Integer.MAX_VALUE)
    public double scale;

    public ConfigExBasics(double scale) {
        this.scale = scale;
    }
}
