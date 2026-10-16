package icbm.classic.config.missile;

import icbm.classic.lib.config.Config;

public class ConfigBomblet {

    @Config.Name("impact_damage")
    @Config.Comment("Damage applied on direct impact, scaled by velocity")
    @Config.RangeDouble(min = 1)
    public float impactDamage = 3F;

    @Config.Name("health")
    @Config.Comment("Hearts of damage before bomblet is destroyed")
    @Config.RangeInt(min = 1)
    public int health = 5;
}
