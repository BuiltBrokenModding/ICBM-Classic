package icbm.classic.config.blast.types;

import icbm.classic.ICBMConstants;
import icbm.classic.lib.config.Config;

public class ConfigContagious {

    @Config.Comment("Size (meters) of the gas cloud")
    @Config.Name("size")
    @Config.RangeInt(min = 1)
    public int size = 20;

    @Config.Comment("Duration (ticks) of the gas cloud")
    @Config.Name("duration")
    @Config.RangeInt(min = 1)
    public int duration = ICBMConstants.TICKS_SEC * 30;

    @Config.Comment("Minimal toxicity built up on an entity before starting bio damage. User gets 4 points per second (1 per 5 server ticks)")
    @Config.Name("toxicity_start")
    @Config.RangeInt(min = 1)
    public int toxicityBuildup = 10;

    @Config.Comment("Scale to use for calculating damage once toxicity starts. Damage is applied every 5 ticks. damage = toxicity * scale")
    @Config.Name("toxicity_scale")
    @Config.RangeDouble(min = 0)
    public float toxicityScale = 0.05f;

    @Config.Comment("Minimal damage to apply once toxicity starts")
    @Config.Name("toxicity_min_damage")
    @Config.RangeDouble(min = 0)
    public float toxicityMinDamage = 1f;
}
