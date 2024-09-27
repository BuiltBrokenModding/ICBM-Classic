package icbm.classic.config;

import icbm.classic.ICBMConstants;
import icbm.classic.config.machines.ConfigSpikes;
import net.minecraftforge.common.config.Config;

@Config(modid = ICBMConstants.DOMAIN, name = "icbmclassic/blocks")
@Config.LangKey("config.icbmclassic:blocks.title")
public class ConfigBlocks
{
    @Config.Name("spike_blocks")
    @Config.Comment("Config for spike blocks")
    public static ConfigSpikes spikes = new ConfigSpikes();
}
