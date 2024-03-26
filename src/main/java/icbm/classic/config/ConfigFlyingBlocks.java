package icbm.classic.config;

import icbm.classic.IcbmConstants;
import net.neoforged.common.config.Config;

@Config(modid = IcbmConstants.MOD_ID, name = "icbmclassic/flying_blocks")
@Config.LangKey("config.icbmclassic:flying_blocks")
public class ConfigFlyingBlocks {
    @Config.Name("enable")
    @Config.Comment("Enables flying blocks, set to false to prevent additional usage in blasts. Doesn't remove existing or prevent other mods from spawning more.")
    public static boolean ENABLED = true;

    @Config.Name("block_replacements")
    @Config.Comment("Replacements to use, Format: 'blockStateA | blockStateB' Docs: https://github.com/BuiltBrokenModding/ICBM-Classic/wiki/config-block-states")
    public String[] REPLACEMENTS = new String[]{"minecraft:water | minecraft:ice"};

    @Config.Name("block_ban_allow")
    public static BanList BAN_ALLOW = new BanList();

    public static class BanList {

        @Config.Name("ban")
        @Config.Comment("Set to true to ban all blocks contained. False to use as allow list")
        public boolean BAN = true;

        @Config.Name("list")
        @Config.Comment("Block/BlastState names. Docs: https://github.com/BuiltBrokenModding/ICBM-Classic/wiki/config-block-states")
        public String[] BLOCK_STATES = new String[]{"minecraft:fire"};
    }
}
