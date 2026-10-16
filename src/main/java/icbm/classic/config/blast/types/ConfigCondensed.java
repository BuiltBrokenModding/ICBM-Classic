package icbm.classic.config.blast.types;

import icbm.classic.lib.config.Config;

public class ConfigCondensed {

    @Config.Name("energy_scale")
    @Config.Comment("Amount of energy to start the explosion. Higher values destroy more blocks but also increases lag. Values over 10 will start to see skipped blocks due vanilla's TNT logic.")
    @Config.RangeDouble(min = 1)
    public double energyScale = 6;

    @Config.Name("damage")
    @Config.Comment("Damage to apply to entities")
    @Config.RangeInt(min = 1)
    public int damage = 10;
}
