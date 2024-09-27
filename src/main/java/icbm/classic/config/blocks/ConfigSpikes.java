package icbm.classic.config.blocks;

import net.minecraftforge.common.config.Config;

public class ConfigSpikes {

    @Config.Name("normal_spike_damage")
    @Config.RangeDouble(min = 1, max = Integer.MAX_VALUE)
    public float normalDamage = 1;

    @Config.Name("poison_spike_damage")
    @Config.RangeDouble(min = 1, max = Integer.MAX_VALUE)
    public float poisonDamage = 1;

    @Config.Name("fire_spike_damage")
    @Config.RangeDouble(min = 1, max = Integer.MAX_VALUE)
    public float fireDamage = 1;
}
