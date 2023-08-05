package icbm.classic.config;

import icbm.classic.ICBMConstants;
import net.minecraftforge.common.config.Config;

@Config(modid = ICBMConstants.DOMAIN, name = "icbmclassic/flying_blocks")
@Config.LangKey("config.icbmclassic:flying_blocks")
public class ConfigFlyingBlocks
{
    @Config.Name("enable")
    @Config.Comment("Enables flying blocks, set to false to prevent additional usage in blasts. Doesn't remove existing or prevent other mods from spawning more.")
    public static boolean ENABLED = true;

    @Config.Name("block_replacements")
    public static BlockReplacements REPLACE = new BlockReplacements();

    @Config.Name("block_ban_allow")
    public static BanList BAN_ALLOW = new BanList();

    public static class FlyingBlockReplacement {

        @Config.Name("input")
        public static String input;

        @Config.Name("output")
        public static String output;
    }

    public static class BlockReplacements {

        @Config.Name("list")
        @Config.Comment("Replacements to use, Format: blockStateA | blockStateB")
        public String[] BLOCK_STATES = new String[]{"minecraft:water | minecraft:ice"};
    }

    public static class BanList {

        @Config.Name("ban")
        @Config.Comment("Set to true to ban all blocks contained. False to use as allow list")
        public boolean BAN = true;

        @Config.Name("list")
        @Config.Comment("Block/BlastState names. Ex: 'minecraft:stone or minecraft:stone@2' supports black listing mods via 'mod_id:~'")
        public String[] BLOCK_STATES = new String[]{"minecraft:fire"};
    }
}
