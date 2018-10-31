package icbm.classic.config;

import icbm.classic.ICBMClassic;
import net.minecraftforge.common.config.Config;

@Config(modid = ICBMClassic.DOMAIN, name = "icbmclassic/mods/nuclearcraft")
@Config.LangKey("config.icbmclassic:nuclearcraft.title")
public class ConfigNuclearCraft
{
    @Config.Name("disable")
    @Config.Comment("Set to true to disable NuclearCraft support. Requires restart to take full effect.")
    public static boolean DISABLED = false;

    @Config.Name("nukecausesrads")
    @Config.Comment("Set to true to have the nuclear blasts add NuclearCraft radiation to chunks. Requires restart to take full effect.")
    public static boolean NUKESCAUSERADS = true;

    @Config.Name("radsamount")
    @Config.Comment("The amount of radiation added to chunks when a nuke is detonated.")
    public static double RADS = 0.25D;
}
