package icbm.classic.config;

import icbm.classic.ICBMConstants;
import net.minecraftforge.common.config.Config;

/**
 *
 * Created by Dark(DarkGuardsman, Robert) on 3/20/2018.
 */
@Config(modid = ICBMConstants.DOMAIN, name = "icbmclassic/item")
@Config.LangKey("config.icbmclassic:item.title")
public class ConfigItems
{
    @Config.Name("enable_crafting_items")
    @Config.Comment("Enables crafting items required to make most of the content. Only disable if other mods provide the resources or custom recipes are implemented.")
    @Config.RequiresMcRestart
    public static boolean ENABLE_CRAFTING_ITEMS = true;

    @Config.Name("enable_circuits")
    @Config.Comment("Whether or not to enable the mod's circuit items which are a crafting material. Only disable if you have a replacement or custom recipes.")
    @Config.RequiresMcRestart
    public static boolean ENABLE_CIRCUIT_ITEMS = true;

    @Config.Name("enable_ingots")
    @Config.Comment("Whether or not to enable the mod's ingot items which are a crafting material. Only disable if you have a replacement or custom recipes.")
    @Config.RequiresMcRestart
    public static boolean ENABLE_INGOTS_ITEMS = true;

    @Config.Name("enable_plates")
    @Config.Comment("Whether or not to enable the mod's plate items which are a crafting material. Only disable if you have a replacement or custom recipes.")
    @Config.RequiresMcRestart
    public static boolean ENABLE_PLATES_ITEMS = true;

    @Config.Name("enable_wires")
    @Config.Comment("Whether or not to enable the mod's wires items which are a crafting material. Only disable if you have a replacement or custom recipes.")
    @Config.RequiresMcRestart
    public static boolean ENABLE_WIRES_ITEMS = true;

    @Config.Name("enable_battery")
    @Config.Comment("Whether or not to enable the mod's battery item which is a crafting material. Only disable if you have a replacement or custom recipes.")
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
