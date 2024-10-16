package icbm.classic.config.blast.types;

import net.minecraftforge.common.config.Config;

public class ConfigEmp {

    @Config.LangKey("config.icbmclassic:blast.scale.title")
    @Config.Comment("Scale of the explosive; This isn't always max size but a multiplier used in calculations.")
    @Config.RangeDouble(min = 1, max = Integer.MAX_VALUE)
    public double scale = 50;

    @Config.Name("replacements")
    public BlockReplacements replacements = new BlockReplacements();

    public static class BlockReplacements {

        @Config.Name("list")
        @Config.Comment("Sets which block to use when replacing a block during an emp event Docs: https://github.com/BuiltBrokenModding/ICBM-Classic/wiki/config-block-states")
        public String[] blockStates = new String[]{};
    }

    @Config.Name("allow_creeper_charging")
    @Config.Comment("Should a lighting effect be applied to the creeper to super charge it due to EMP effect?")
    public boolean ALLOW_LIGHTING_CREEPER = true;

    @Config.Name("missiles")
    @Config.Comment("EMP Settings for missiles")
    public final Missiles missiles = new Missiles();

    public static class Missiles {

        @Config.Name("enabled")
        @Config.Comment("Should EMP work on missiles")
        public boolean enabled = true;

        @Config.Name("engine_kill_chance")
        @Config.Comment("Chance the engine will be disabled, random <= chance")
        @Config.RangeDouble(min = 0, max = 1)
        public float engineKillChance = 0.8f;

        @Config.Name("engine_fuel_blow_chance")
        @Config.Comment("Chance that when the missile is killed the fuel will detonate, random <= chance")
        @Config.RangeDouble(min = 0, max = 1)
        public float engineFullBlowChance = 1f;

        @Config.Name("engine_fuel_blow_strength")
        @Config.Comment("Power of the explosion from the fuel")
        @Config.RangeDouble(min = 0.1)
        public float engineFullBlowStrength = 1f;

        @Config.Name("missile_kill_chance")
        @Config.Comment("Chance the missile will be destroyed, random <= chance")
        @Config.RangeDouble(min = 0, max = 1)
        public float missileKillChance = 0.5f;

        @Config.Name("missile_trigger_chance")
        @Config.Comment("Chance the missile explosive will be triggered, random <= chance")
        @Config.RangeDouble(min = 0, max = 1)
        public float missileTriggerChance = 0.1f;
    }

    @Config.Name("allow_entity_inventory")
    @Config.Comment("Should EMP effect run on entity inventories? (Eg. Player, Cart)")
    public boolean ALLOW_ENTITY_INVENTORY = true;

    @Config.Name("allow_tile_inventory")
    @Config.Comment("Should EMP effect run on block/tile inventories? (Eg. Chest, Hopper, Machine)")
    public boolean ALLOW_TILE_INVENTORY = true;

    @Config.Name("allow_item_inventory")
    @Config.Comment("Should EMP effect run on item inventories? (Eg. Bag, Backpack)")
    public boolean ALLOW_ITEM_INVENTORY = true;

    @Config.Name("allow_ground_items")
    @Config.Comment("Should EMP effect run on items dropped on the ground?")
    public boolean ALLOW_GROUND_ITEMS = true;

    @Config.Name("allow_draining_energy_entity")
    @Config.Comment("Should EMP effect drain energy entities that do not support EMP effect directly?")
    public boolean DRAIN_ENERGY_ENTITY = true;

    @Config.Name("allow_draining_energy_items")
    @Config.Comment("Should EMP effect drain energy items that do not support EMP effect directly?")
    public boolean DRAIN_ENERGY_ITEMS = true;

    @Config.Name("allow_draining_energy_tiles")
    @Config.Comment("Should EMP effect drain energy tiles that do not support EMP effect directly?")
    public boolean DRAIN_ENERGY_TILES = true;

    @Config.Name("allow_entities")
    @Config.Comment("Should EMP effect run on entities?")
    public boolean ALLOW_ENTITY = true;

    @Config.Name("allow_tiles")
    @Config.Comment("Should EMP effect run on blocks and tiles?")
    public boolean ALLOW_TILES = true;
}
