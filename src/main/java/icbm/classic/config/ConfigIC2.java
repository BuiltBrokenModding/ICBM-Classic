package icbm.classic.config;

import icbm.classic.ICBMClassic;
import net.minecraftforge.common.config.Config;

/**
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 2/22/2018.
 */
@Config(modid = ICBMClassic.DOMAIN, name = "icbmclassic/mods/ic2")
@Config.LangKey("config.icbmclassic:ic2.title")
public class ConfigIC2
{
    @Config.Name("from_ic2")
    @Config.Comment("How much (EU) IC2 energy to turn into (FE) Forge energy")
    @Config.RangeInt(min = 0)
    public static double FROM_IC2 = 4D; //Matched with Mekanism

    @Config.Name("disable")
    @Config.Comment("Set to true to disable IC2 support. Requires restart to take full effect.")
    public static boolean DISABLED = false;
}
