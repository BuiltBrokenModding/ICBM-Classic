package icbm.classic.config;

import icbm.classic.ICBMClassic;
import net.minecraftforge.common.config.Config;

/**
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 3/20/2018.
 */
@Config(modid = ICBMClassic.DOMAIN, name = "icbmclassic/item")
@Config.LangKey("config.icbmclassic:item.title")
public class ConfigItems
{
    @Config.Name("enable_crafting_items")
    @Config.Comment("Enables crafting items required to make most of the content. Only disable if other mods provide the resources or custom recipes are implemented.")
    @Config.RequiresMcRestart
    public static boolean ENABLE_CRAFTING_ITEMS = true;

    @Config.Name("enable_circuits")
    @Config.RequiresMcRestart
    public static boolean ENABLE_CIRCUIT_ITEMS = true;

    @Config.Name("enable_ingots")
    @Config.RequiresMcRestart
    public static boolean ENABLE_INGOTS_ITEMS = true;

    @Config.Name("enable_plates")
    @Config.RequiresMcRestart
    public static boolean ENABLE_PLATES_ITEMS = true;

    @Config.Name("enable_wires")
    @Config.RequiresMcRestart
    public static boolean ENABLE_WIRES_ITEMS = true;

    @Config.Name("enable_battery")
    @Config.RequiresMcRestart
    public static boolean ENABLE_BATTERY = true;

    @Config.Name("enable_sulfur_drops")
    @Config.Comment("Enables dropping sulfur from creepers. Use a loot table mod to change the drop rates.")
    @Config.RequiresMcRestart
    public static boolean ENABLE_SULFUR_LOOT_DROPS = true;

    @Config.Name("enable_loot_drops")
    @Config.Comment("Enables finding crafting items inside of chests as loot. Use a loot table mod to change the drop rates.")
    @Config.RequiresMcRestart
    public static boolean ENABLE_LOOT_DROPS = true;
}
