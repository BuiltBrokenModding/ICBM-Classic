package icbm.classic.config.blocks;

import net.minecraftforge.common.config.Config;

public class ConfigRadioactive {

    @Config.Name("decay_chance")
    @Config.Comment("Chance for radioactive decay effects to be triggered")
    @Config.RangeDouble(min = 0, max = 1)
    public float decayChance = 0.85f;

    @Config.Name("decay_effect_range")
    @Config.Comment("Range (meters) for effects to be applied")
    @Config.RangeInt(min = 1)
    public int decayRange = 5;

    @Config.Name("decay_delay")
    @Config.Comment("Delay (20 ticks per second) between running decay chance calculation and effects. Set to zero to disable, if previously zero requires world restart for old blocks to tick.")
    @Config.RangeInt(min = 1)
    public int decayDelay = 5;

    @Config.Name("decay_damage")
    @Config.Comment("Damage to apply to entities caught in decay range")
    @Config.RangeDouble(min = 1)
    public float decayDamage = 2;
}
