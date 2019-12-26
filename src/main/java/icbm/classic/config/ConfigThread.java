package icbm.classic.config;

import icbm.classic.ICBMConstants;
import net.minecraftforge.common.config.Config;

/**
 * Created by Dark(DarkGuardsman, Robert) on 2/10/2019.
 */
@Config(modid = ICBMConstants.DOMAIN, name = "icbmclassic/thread")
@Config.LangKey("config.icbmclassic:thread.title")
public class ConfigThread
{
    @Config.Name("thread_count")
    @Config.Comment("Number of worker threads to run to handle blast calculations. " +
            "Try to only match 50% of the number of cores your machine can support. " +
            "Otherwise the main game thread will slow down while the workers are processing. " +
            "Which is counter to the reason threads exist.")
    @Config.RangeInt(min = 1, max = 8)
    public static int THREAD_COUNT = 1;
}
