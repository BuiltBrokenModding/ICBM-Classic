package icbm.classic.config.blocks;

import net.minecraftforge.common.config.Config;

public class ConfigRadioactive {

    @Config.Name("decay_delay")
    @Config.Comment("Delay in ticks (20 ticks per second) between running decay chance calculation and effects. Set to zero to disable, if previously zero requires world restart for old blocks to tick.")
    @Config.RangeInt(min = 1)
    public int decayDelay = 5;

    @Config.Name("decay_block_chance")
    @Config.Comment("Chance (random < value) for radioactive block to convert into harmless blocks, EX: Radioactive Stone -> Stone")
    @Config.RangeDouble(min = 0, max = 1)
    public float decayBlockChance = 0.01f;

    @Config.Name("decay_effect_chance")
    @Config.Comment("Chance (random < value) for radioactive decay effects to be triggered per entity")
    @Config.RangeDouble(min = 0, max = 1)
    public float decayEffectChance = 0.85f;

    @Config.Name("decay_effect_range")
    @Config.Comment("Range (meters) for effects to be applied")
    @Config.RangeInt(min = 1)
    public int decayEffectRange = 5;


    @Config.Name("decay_effect_damage")
    @Config.Comment("Damage to apply to entities caught in decay range")
    @Config.RangeDouble(min = 1)
    public float decayEffectDamage = 2;

    @Config.Name("decay_effect_wither_duration")
    @Config.Comment("Time in ticks (20 ticks per second) to apply withering effect. Set to zero to disable")
    @Config.RangeInt(min = 0)
    public int decayWitherDuration = 20;

}
