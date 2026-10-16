package icbm.classic.config.blast.types;

import icbm.classic.lib.config.Config;

public class ConfigShrapnel {

    @Config.Name("fragments")
    @Config.Comment("Amount of rotation in yaw and pitch to spawn fragments. total = fragments * fragments")
    @Config.RangeInt(min = 1)
    public int fragments = 30;

    @Config.Name("damage")
    @Config.Comment("Damage to apply to entities")
    @Config.RangeInt(min = 0)
    public int damage = 11;
}
