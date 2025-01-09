package icbm.classic.config;

//@Config(modid = ICBMConstants.DOMAIN, name = "icbmclassic/flying_blocks")
//@Config.LangKey("config.icbmclassic:flying_blocks")
public class ConfigFlyingBlocks
{
    //@Config.Name("enable")
    //@Config.Comment("Enables flying blocks, set to false to prevent additional usage in blasts. Doesn't remove existing or prevent other mods from spawning more.")
    public static boolean enabled = true;

    ////@Config.Name("block_replacements") TODO implement
    ////@Config.Comment("Replacements to use, Format: 'blockStateA | blockStateB' Docs: https://github.com/BuiltBrokenModding/ICBM-Classic/wiki/config-block-states")
    //public String[] replacements = new String[]{"minecraft:water | minecraft:ice"};

    //@Config.Name("block_ban_allow")
    public static BanList banAllow = new BanList();

    //@Config.Name("damage_scale")
    //@Config.Comment("Damage to apply, scaled by velocity")
    public static float damageScale = 2f;

    public static class BanList {

        //@Config.Name("ban")
        //@Config.Comment("Set to true to ban all blocks contained. False to use as allow list")
        public boolean ban = true;

        //@Config.Name("list")
        //@Config.Comment("Block/BlastState names. Docs: https://github.com/BuiltBrokenModding/ICBM-Classic/wiki/config-block-states")
        public String[] blockStates = new String[]{"minecraft:fire"};
    }
}
