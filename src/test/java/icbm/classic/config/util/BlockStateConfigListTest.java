package icbm.classic.config.util;

import com.google.common.collect.Lists;
import net.minecraft.init.Blocks;
import net.minecraft.init.Bootstrap;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class BlockStateConfigListTest {

    @BeforeAll
    static void beforeAll() {
        Bootstrap.register();
    }

    @Nested
    class BlockPathEndsWithTests {
        @Test
        void endsWith_pass() {
            final BlockStateConfigList configList = new BlockStateConfigList("test", null);
            Assertions.assertTrue(configList.blockPathEndsWith(Blocks.BONE_BLOCK, "block"));
        }

        @Test
        void matches_pass() {
            final BlockStateConfigList configList = new BlockStateConfigList("test", null);
            Assertions.assertTrue(configList.blockPathEndsWith(Blocks.BONE_BLOCK, "bone_block"));
        }

        @Test
        void notEnds_fail() {
            final BlockStateConfigList configList = new BlockStateConfigList("test", null);
            Assertions.assertFalse(configList.blockPathEndsWith(Blocks.BONE_BLOCK, "bone"));
        }
    }

    @Nested
    class BlockPathStartsWithTests {
        @Test
        void startsWith_pass() {
            final BlockStateConfigList configList = new BlockStateConfigList("test", null);
            Assertions.assertTrue(configList.blockPathStartsWith(Blocks.BONE_BLOCK, "bone"));
        }

        @Test
        void matches_pass() {
            final BlockStateConfigList configList = new BlockStateConfigList("test", null);
            Assertions.assertTrue(configList.blockPathStartsWith(Blocks.BONE_BLOCK, "bone_block"));
        }

        @Test
        void notStarts_fail() {
            final BlockStateConfigList configList = new BlockStateConfigList("test", null);
            Assertions.assertFalse(configList.blockPathStartsWith(Blocks.BONE_BLOCK, "block"));
        }
    }

    @Nested
    class HandleFuzzyBlocksTests {

        @Test
        void modEntry_addToModList() {
            final BlockStateConfigList configList = new BlockStateConfigList("test", null);
            configList.handleFuzzyBlocks("icbm:~");
            Assertions.assertEquals(Lists.newArrayList("icbm"), configList.mods);
        }

        @Test
        void blockFuzzyStart_addFuzzyBlockList() {
            final BlockStateConfigList configList = new BlockStateConfigList("test", null);
            configList.handleFuzzyBlocks("minecraft:iron~");

            // Validate we only added 1 entry
            Assertions.assertTrue(configList.fuzzyBlockChecks.containsKey("minecraft"));
            Assertions.assertEquals(1, configList.fuzzyBlockChecks.get("minecraft").size());

            // Validate lambda works as expected
            Assertions.assertTrue(configList.fuzzyBlockChecks.get("minecraft").get(0).apply(Blocks.IRON_BLOCK));
            Assertions.assertTrue(configList.fuzzyBlockChecks.get("minecraft").get(0).apply(Blocks.IRON_DOOR));
            Assertions.assertTrue(configList.fuzzyBlockChecks.get("minecraft").get(0).apply(Blocks.IRON_BARS));

            Assertions.assertFalse(configList.fuzzyBlockChecks.get("minecraft").get(0).apply(Blocks.DIRT));
        }

        @Test
        void blockFuzzyEnds_addFuzzyBlockList() {
            final BlockStateConfigList configList = new BlockStateConfigList("test", null);
            configList.handleFuzzyBlocks("minecraft:~door");

            // Validate we only added 1 entry
            Assertions.assertTrue(configList.fuzzyBlockChecks.containsKey("minecraft"));
            Assertions.assertEquals(1, configList.fuzzyBlockChecks.get("minecraft").size());

            // Validate lambda works as expected
            Assertions.assertTrue(configList.fuzzyBlockChecks.get("minecraft").get(0).apply(Blocks.DARK_OAK_DOOR));
            Assertions.assertTrue(configList.fuzzyBlockChecks.get("minecraft").get(0).apply(Blocks.IRON_DOOR));
            Assertions.assertTrue(configList.fuzzyBlockChecks.get("minecraft").get(0).apply(Blocks.OAK_DOOR));

            Assertions.assertFalse(configList.fuzzyBlockChecks.get("minecraft").get(0).apply(Blocks.DIRT));
        }
    }
}