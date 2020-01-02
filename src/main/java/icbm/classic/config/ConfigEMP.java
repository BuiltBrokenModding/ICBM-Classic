package icbm.classic.config;

import icbm.classic.ICBMConstants;
import net.minecraftforge.common.config.Config;

/**
 *
 * Created by Dark(DarkGuardsman, Robert) on 3/12/2018.
 */
@Config(modid = ICBMConstants.DOMAIN, name = "icbmclassic/emp")
@Config.LangKey("config.icbmclassic:emp.title")
public class ConfigEMP
{
    @Config.Name("allow_creeper_charging")
    @Config.Comment("Should a lighting effect be applied to the creeper to super charge it due to EMP effect?")
    public static boolean ALLOW_LIGHTING_CREEPER = true;

    @Config.Name("allow_missiles_destroy")
    @Config.Comment("Should the EMP effect kill missile entities mid flight?")
    public static boolean ALLOW_MISSILE_DESTROY = true;

    @Config.Name("allow_missiles_drop")
    @Config.Comment("Should EMP effect trigger missiles entities to drop as items when killed?")
    public static boolean ALLOW_MISSILE_DROPS = true;

    @Config.Name("allow_entity_inventory")
    @Config.Comment("Should EMP effect run on entity inventories? (Eg. Player, Cart)")
    public static boolean ALLOW_ENTITY_INVENTORY = true;

    @Config.Name("allow_tile_inventory")
    @Config.Comment("Should EMP effect run on block/tile inventories? (Eg. Chest, Hopper, Machine)")
    public static boolean ALLOW_TILE_INVENTORY = true;

    @Config.Name("allow_item_inventory")
    @Config.Comment("Should EMP effect run on item inventories? (Eg. Bag, Backpack)")
    public static boolean ALLOW_ITEM_INVENTORY = true;

    @Config.Name("allow_ground_items")
    @Config.Comment("Should EMP effect run on items dropped on the ground?")
    public static boolean ALLOW_GROUND_ITEMS = true;

    @Config.Name("allow_draining_energy_entity")
    @Config.Comment("Should EMP effect drain energy entities that do not support EMP effect directly?")
    public static boolean DRAIN_ENERGY_ENTITY = true;

    @Config.Name("allow_draining_energy_items")
    @Config.Comment("Should EMP effect drain energy items that do not support EMP effect directly?")
    public static boolean DRAIN_ENERGY_ITEMS = true;

    @Config.Name("allow_draining_energy_tiles")
    @Config.Comment("Should EMP effect drain energy tiles that do not support EMP effect directly?")
    public static boolean DRAIN_ENERGY_TILES = true;

    @Config.Name("allow_entities")
    @Config.Comment("Should EMP effect run on entities?")
    public static boolean ALLOW_ENTITY = true;

    @Config.Name("allow_tiles")
    @Config.Comment("Should EMP effect run on blocks and tiles?")
    public static boolean ALLOW_TILES = true;
}
