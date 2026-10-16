package icbm.classic.config.blast.types;

import icbm.classic.lib.config.Config;

public class ConfigAntimatter {

    @Config.Name("break_unbreakable")
    @Config.Comment("Should antimatter ignore hardness checks for unbreakable blocks (bedrock, wards)")
    public boolean damageUnbreakable = true;

    @Config.Name("damage_on_redmatter_kill")
    @Config.Comment("Whether or not antimatter damages blocks/entities when detonating and killing redmatter")
    public boolean damageOnRedmatterKill = false;

    @Config.Comment("Size (meters) of the blast")
    @Config.Name("size")
    @Config.RangeInt(min = 1)
    public int size = 55;

    @Config.Comment("Damage to apply to entities. Is applied at start and end of blast.")
    @Config.Name("damage")
    @Config.RangeDouble(min = 1)
    public float damage = Integer.MAX_VALUE;
}
