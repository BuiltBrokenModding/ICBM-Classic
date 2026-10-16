package icbm.classic.config.blast.types;

import icbm.classic.lib.config.Config;

public class ConfigBreaching {

    @Config.Name("depth")
    @Config.Comment("How deep (meters) the breaching charge go before zeroing out energy left.")
    @Config.RangeInt(min = 1)
    public int depth = 7;

    @Config.Name("size")
    @Config.Comment("Distance (meters) from center to create edge of hole. Hole will be (s + 1 + s) width & height. So for a value of 1 this will make a 3x3 hole.")
    @Config.RangeInt(min = 0)
    public int size = 1;

    @Config.Name("damage")
    @Config.Comment("Damage to apply to entities")
    @Config.RangeInt(min = 0)
    public int damage = 13;

    @Config.Name("energy")
    @Config.Comment("Explosive energy to start each blast depth line.")
    @Config.RangeDouble(min = 0)
    public float energy = 15376; //energy needed to break several layers of high tier concrete

    @Config.Name("energy_scale_distance")
    @Config.Comment("Scale factor to reduce starting energy for each block away from center. e = e_start - (e_start * (w + h) * scale)")
    @Config.RangeDouble(min = 0)
    public float energyDistanceScale = 0.25f;

    @Config.Name("energy_cost_distance")
    @Config.Comment("Energy cost percentage for traveling through a block with no explosive resistance")
    @Config.RangeDouble(min = 0, max = 1)
    public float energyCostDistance = 1 - 0.65f;
}
