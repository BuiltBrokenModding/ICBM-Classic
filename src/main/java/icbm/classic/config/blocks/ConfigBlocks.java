package icbm.classic.config.blocks;

import icbm.classic.ICBMConstants;
import icbm.classic.lib.config.Config;

@Config(modid = ICBMConstants.DOMAIN, name = "icbmclassic/blocks")
@Config.LangKey("config.icbmclassic:blocks.title")
public class ConfigBlocks
{
    @Config.Name("spikes")
    @Config.Comment("Config for spike blocks")
    public static ConfigSpikes spikes = new ConfigSpikes();

    @Config.Name("radioactive")
    @Config.Comment("Config for radioactive blocks")
    public static ConfigRadioactive radioactive = new ConfigRadioactive();
}
