package icbm.classic.config.util;

import com.google.common.collect.Lists;
import net.minecraft.block.BlockStone;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.init.Bootstrap;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.HashSet;

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
            Assertions.assertTrue(configList.handleFuzzyBlocks("icbm:~"));
            Assertions.assertEquals(Lists.newArrayList("icbm"), configList.mods);
        }

        @Test
        void blockFuzzyStart_addFuzzyBlockList() {
            final BlockStateConfigList configList = new BlockStateConfigList("test", null);
            Assertions.assertTrue(configList.handleFuzzyBlocks("minecraft:iron~"));

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
            Assertions.assertTrue(configList.handleFuzzyBlocks("minecraft:~door"));

            // Validate we only added 1 entry
            Assertions.assertTrue(configList.fuzzyBlockChecks.containsKey("minecraft"));
            Assertions.assertEquals(1, configList.fuzzyBlockChecks.get("minecraft").size());

            // Validate lambda works as expected
            Assertions.assertTrue(configList.fuzzyBlockChecks.get("minecraft").get(0).apply(Blocks.DARK_OAK_DOOR));
            Assertions.assertTrue(configList.fuzzyBlockChecks.get("minecraft").get(0).apply(Blocks.IRON_DOOR));
            Assertions.assertTrue(configList.fuzzyBlockChecks.get("minecraft").get(0).apply(Blocks.OAK_DOOR));

            Assertions.assertFalse(configList.fuzzyBlockChecks.get("minecraft").get(0).apply(Blocks.DIRT));
        }

        @ParameterizedTest(name = "{index}: invalid format \"{0}\"")
        @ValueSource(strings = {
            // tilda in wrong spot
            "minecraft:a~b",
            "~minecraft:ab",
            "minecraft~:ab",
            "minec~raft:ab",

            // Missing domain
            ":ab~",
            ":~ab",
            ":~",

            // extra colons
            "minecraft::stone",
            "minecraft:stone:~",
            "minecraft:stone:stone~",
            "minecraft:stone:~stone",

            // Missing colon
            "minecraftstone",

            // Extra tildas
            "minecraft:stone~~",
            "minecraft: ~~stone"
        })
        void badFormats(String value) {
            final BlockStateConfigList configList = new BlockStateConfigList("test", null);
            Assertions.assertFalse(configList.handleFuzzyBlocks(value));
            Assertions.assertEquals(Lists.newArrayList(), configList.mods);
        }
    }

    @Nested
    class HandleMetaDataTests {

        @Test
        void badFormat_missingColon() {
            final BlockStateConfigList configList = new BlockStateConfigList("test", null);
            Assertions.assertFalse(configList.handleMetaData("minecraftstone@2"));

            // Would expect nothing to be added
            Assertions.assertEquals(new HashSet(), configList.blockStates);
        }

        @Test
        void badFormat_doubleColon() {
            final BlockStateConfigList configList = new BlockStateConfigList("test", null);
            Assertions.assertFalse(configList.handleMetaData("minecraft::stone@2"));

            // Would expect nothing to be added
            Assertions.assertEquals(new HashSet(), configList.blockStates);
        }

        @Test
        void badFormat_doubleColon2() {
            final BlockStateConfigList configList = new BlockStateConfigList("test", null);
            Assertions.assertFalse(configList.handleMetaData("minecraft:stone:@2"));

            // Would expect nothing to be added
            Assertions.assertEquals(new HashSet(), configList.blockStates);
        }

        @Test
        void badFormat_missingNumber() {
            final BlockStateConfigList configList = new BlockStateConfigList("test", null);
            Assertions.assertFalse(configList.handleMetaData("minecraft:stone@"));

            // Would expect nothing to be added
            Assertions.assertEquals(new HashSet(), configList.blockStates);
        }

        @Test
        void badFormat_onlyHasNumber() {
            final BlockStateConfigList configList = new BlockStateConfigList("test", null);
            Assertions.assertFalse(configList.handleMetaData("@2"));

            // Would expect nothing to be added
            Assertions.assertEquals(new HashSet(), configList.blockStates);
        }

        @Test
        void badFormat_missingDomain() {
            final BlockStateConfigList configList = new BlockStateConfigList("test", null);
            Assertions.assertFalse(configList.handleMetaData(":stone@2"));

            // Would expect nothing to be added
            Assertions.assertEquals(new HashSet(), configList.blockStates);
        }

        @Test
        void badFormat_missingResource() {
            final BlockStateConfigList configList = new BlockStateConfigList("test", null);
            Assertions.assertFalse(configList.handleMetaData("minecraft:@2"));

            // Would expect nothing to be added
            Assertions.assertEquals(new HashSet(), configList.blockStates);
        }

        @Test
        void blockWithMetadata_notMatching() {
            final BlockStateConfigList configList = new BlockStateConfigList("test", null);
            Assertions.assertFalse(configList.handleMetaData("minecraft:stone@20"));

            // Would expect nothing to be added
            Assertions.assertEquals(new HashSet(), configList.blockStates);
        }

        @Test
        void blockWithMetadata_blockNotFound() {
            final BlockStateConfigList configList = new BlockStateConfigList("test", null);
            Assertions.assertFalse(configList.handleMetaData("minecraft:tower@2"));
            //TODO find a way to confirm it is false due to missing block, as this produced a false-positive for 'block == null' almost missing that forgeReg returns Blocks.air for missing entries

            // Would expect nothing to be added
            Assertions.assertEquals(new HashSet(), configList.blockStates);
        }

        // TODO missing blockState, can't normally happen so need to register a broken block or use mocks to break it

        @Test
        void blockWithMetadata_matching() {
            final BlockStateConfigList configList = new BlockStateConfigList("test", null);
            Assertions.assertTrue(configList.handleMetaData("minecraft:stone@2"));

            HashSet<IBlockState> sets = new HashSet();
            sets.add(Blocks.STONE.getDefaultState().withProperty(BlockStone.VARIANT, BlockStone.EnumType.GRANITE_SMOOTH));

            // Would expect exact match
            Assertions.assertEquals(sets, configList.blockStates);
        }

        // TODO allows extra spaces, do several with param test
    }
}