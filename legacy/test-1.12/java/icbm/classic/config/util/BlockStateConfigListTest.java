package icbm.classic.config.util;

import com.google.common.collect.Lists;
import net.minecraft.block.Block;
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
import java.util.List;

class BlockStateConfigListTest {

    @BeforeAll
    static void beforeAll() {
        Bootstrap.register();
    }

    @Nested
    class HandleSimpleBlockTests {
        @Test
        void matchingBlock_noMetaData() {
            final BlockStateConfigListOld configList = new BlockStateConfigListOld("test", null);
            Assertions.assertTrue(configList.handleSimpleBlock("minecraft:dirt"));

            final HashSet<Block> expected = new HashSet<Block>();
            expected.add(Blocks.DIRT);
            Assertions.assertEquals(expected, configList.blocks);
        }

        @Test
        void matchingBlock_withMetaData() {
            final BlockStateConfigListOld configList = new BlockStateConfigListOld("test", null);
            Assertions.assertTrue(configList.handleSimpleBlock("minecraft:stone"));

            final HashSet<Block> expected = new HashSet<Block>();
            expected.add(Blocks.STONE);
            Assertions.assertEquals(expected, configList.blocks);
        }

        @Test
        void noMatchFound() {
            final BlockStateConfigListOld configList = new BlockStateConfigListOld("test", null);
            Assertions.assertFalse(configList.handleSimpleBlock("minecraft:tower"));
            Assertions.assertEquals(new HashSet<Block>(), configList.blocks);
        }

        @Test
        void badFormat_missingResource() {
            final BlockStateConfigListOld configList = new BlockStateConfigListOld("test", null);
            Assertions.assertFalse(configList.handleSimpleBlock("minecraft:"));
            Assertions.assertEquals(new HashSet<Block>(), configList.blocks);
        }

        @Test
        void badFormat_missingDomain() {
            final BlockStateConfigListOld configList = new BlockStateConfigListOld("test", null);
            Assertions.assertFalse(configList.handleSimpleBlock("minecraft:"));
            Assertions.assertEquals(new HashSet<Block>(), configList.blocks);
        }

        @ParameterizedTest(name = "{index}: invalid format \"{0}\"")
        @ValueSource(strings = {
            // Missing domain
            ":stone",

            // Missing resource
            "minecraft:",

            // extra colons
            "minecraft::stone",
            "minecraft:stone:",
            "minecraft:stone:stone",

            // Missing colon
            "minecraftstone"
        })
        void badFormats(String value) {
            final BlockStateConfigListOld configList = new BlockStateConfigListOld("test", null);
            Assertions.assertFalse(configList.handleSimpleBlock(value));
            Assertions.assertEquals(new HashSet<Block>(), configList.blocks);
        }
    }

    @Nested
    class BlockPathEndsWithTests {
        @Test
        void endsWith_pass() {
            final BlockStateConfigListOld configList = new BlockStateConfigListOld("test", null);
            Assertions.assertTrue(configList.blockPathEndsWith(Blocks.BONE_BLOCK, "block"));
        }

        @Test
        void matches_pass() {
            final BlockStateConfigListOld configList = new BlockStateConfigListOld("test", null);
            Assertions.assertTrue(configList.blockPathEndsWith(Blocks.BONE_BLOCK, "bone_block"));
        }

        @Test
        void notEnds_fail() {
            final BlockStateConfigListOld configList = new BlockStateConfigListOld("test", null);
            Assertions.assertFalse(configList.blockPathEndsWith(Blocks.BONE_BLOCK, "bone"));
        }
    }

    @Nested
    class BlockPathStartsWithTests {
        @Test
        void startsWith_pass() {
            final BlockStateConfigListOld configList = new BlockStateConfigListOld("test", null);
            Assertions.assertTrue(configList.blockPathStartsWith(Blocks.BONE_BLOCK, "bone"));
        }

        @Test
        void matches_pass() {
            final BlockStateConfigListOld configList = new BlockStateConfigListOld("test", null);
            Assertions.assertTrue(configList.blockPathStartsWith(Blocks.BONE_BLOCK, "bone_block"));
        }

        @Test
        void notStarts_fail() {
            final BlockStateConfigListOld configList = new BlockStateConfigListOld("test", null);
            Assertions.assertFalse(configList.blockPathStartsWith(Blocks.BONE_BLOCK, "block"));
        }
    }

    @Nested
    class HandleFuzzyBlocksTests {

        @Test
        void modEntry_addToModList() {
            final BlockStateConfigListOld configList = new BlockStateConfigListOld("test", null);
            Assertions.assertTrue(configList.handleFuzzyBlocks("icbm:~"));
            Assertions.assertEquals(Lists.newArrayList("icbm"), configList.mods);
        }

        @Test
        void blockFuzzyStart_addFuzzyBlockList() {
            final BlockStateConfigListOld configList = new BlockStateConfigListOld("test", null);
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
            final BlockStateConfigListOld configList = new BlockStateConfigListOld("test", null);
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
            final BlockStateConfigListOld configList = new BlockStateConfigListOld("test", null);
            Assertions.assertFalse(configList.handleFuzzyBlocks(value));
            Assertions.assertEquals(Lists.newArrayList(), configList.mods);
        }
    }

    @Nested
    class HandleMetaDataTests {

        @Test
        void badFormat_missingColon() {
            final BlockStateConfigListOld configList = new BlockStateConfigListOld("test", null);
            Assertions.assertFalse(configList.handleMetaData("minecraftstone@2"));

            // Would expect nothing to be added
            Assertions.assertEquals(new HashSet(), configList.blockStates);
        }

        @Test
        void badFormat_doubleColon() {
            final BlockStateConfigListOld configList = new BlockStateConfigListOld("test", null);
            Assertions.assertFalse(configList.handleMetaData("minecraft::stone@2"));

            // Would expect nothing to be added
            Assertions.assertEquals(new HashSet(), configList.blockStates);
        }

        @Test
        void badFormat_doubleColon2() {
            final BlockStateConfigListOld configList = new BlockStateConfigListOld("test", null);
            Assertions.assertFalse(configList.handleMetaData("minecraft:stone:@2"));

            // Would expect nothing to be added
            Assertions.assertEquals(new HashSet(), configList.blockStates);
        }

        @Test
        void badFormat_missingNumber() {
            final BlockStateConfigListOld configList = new BlockStateConfigListOld("test", null);
            Assertions.assertFalse(configList.handleMetaData("minecraft:stone@"));

            // Would expect nothing to be added
            Assertions.assertEquals(new HashSet(), configList.blockStates);
        }

        @Test
        void badFormat_onlyHasNumber() {
            final BlockStateConfigListOld configList = new BlockStateConfigListOld("test", null);
            Assertions.assertFalse(configList.handleMetaData("@2"));

            // Would expect nothing to be added
            Assertions.assertEquals(new HashSet(), configList.blockStates);
        }

        @Test
        void badFormat_missingDomain() {
            final BlockStateConfigListOld configList = new BlockStateConfigListOld("test", null);
            Assertions.assertFalse(configList.handleMetaData(":stone@2"));

            // Would expect nothing to be added
            Assertions.assertEquals(new HashSet(), configList.blockStates);
        }

        @Test
        void badFormat_missingResource() {
            final BlockStateConfigListOld configList = new BlockStateConfigListOld("test", null);
            Assertions.assertFalse(configList.handleMetaData("minecraft:@2"));

            // Would expect nothing to be added
            Assertions.assertEquals(new HashSet(), configList.blockStates);
        }

        @Test
        void blockWithMetadata_notMatching() {
            final BlockStateConfigListOld configList = new BlockStateConfigListOld("test", null);
            Assertions.assertFalse(configList.handleMetaData("minecraft:stone@20"));

            // Would expect nothing to be added
            Assertions.assertEquals(new HashSet(), configList.blockStates);
        }

        @Test
        void blockWithMetadata_blockNotFound() {
            final BlockStateConfigListOld configList = new BlockStateConfigListOld("test", null);
            Assertions.assertFalse(configList.handleMetaData("minecraft:tower@2"));
            //TODO find a way to confirm it is false due to missing block, as this produced a false-positive for 'block == null' almost missing that forgeReg returns Blocks.air for missing entries

            // Would expect nothing to be added
            Assertions.assertEquals(new HashSet(), configList.blockStates);
        }

        // TODO missing blockState, can't normally happen so need to register a broken block or use mocks to break it

        @Test
        void blockWithMetadata_matching() {
            final BlockStateConfigListOld configList = new BlockStateConfigListOld("test", null);
            Assertions.assertTrue(configList.handleMetaData("minecraft:stone@2"));

            HashSet<IBlockState> sets = new HashSet();
            sets.add(Blocks.STONE.getDefaultState().withProperty(BlockStone.VARIANT, BlockStone.EnumType.GRANITE_SMOOTH));

            // Would expect exact match
            Assertions.assertEquals(sets, configList.blockStates);
        }

        // TODO allows extra spaces, do several with param test
    }

    @Nested
    class HandleBlockStateTests {
        @Test
        void singleState() {
            final BlockStateConfigListOld configList = new BlockStateConfigListOld("test", null);
            Assertions.assertTrue(configList.handleBlockState("minecraft:stone[variant:diorite]"));

            // Would expect 1 item to be present and contain a mather that fits out block
            Assertions.assertEquals(1, configList.blockStateMatchers.size());
            Assertions.assertTrue(configList.blockStateMatchers.containsKey(Blocks.STONE));
            Assertions.assertEquals(1, configList.blockStateMatchers.get(Blocks.STONE).size());

            final IBlockState target = Blocks.STONE.getDefaultState().withProperty(BlockStone.VARIANT, BlockStone.EnumType.DIORITE);
            Assertions.assertTrue(configList.blockStateMatchers.get(Blocks.STONE).get(0).apply(target));

            final IBlockState target2 = Blocks.STONE.getDefaultState().withProperty(BlockStone.VARIANT, BlockStone.EnumType.GRANITE);
            Assertions.assertFalse(configList.blockStateMatchers.get(Blocks.STONE).get(0).apply(target2));
        }

        @Test
        void singleStateFuzzy_endsWithName() {
            final BlockStateConfigListOld configList = new BlockStateConfigListOld("test", null);
            Assertions.assertTrue(configList.handleBlockState("minecraft:stone[variant:~diorite]"));

            // Would expect 1 item to be present and contain a mather that fits out block
            Assertions.assertEquals(1, configList.blockStateMatchers.size());
            Assertions.assertTrue(configList.blockStateMatchers.containsKey(Blocks.STONE));
            Assertions.assertEquals(1, configList.blockStateMatchers.get(Blocks.STONE).size());

            final IBlockState targetA = Blocks.STONE.getDefaultState().withProperty(BlockStone.VARIANT, BlockStone.EnumType.DIORITE);
            Assertions.assertTrue(configList.blockStateMatchers.get(Blocks.STONE).get(0).apply(targetA));
            final IBlockState targetB = Blocks.STONE.getDefaultState().withProperty(BlockStone.VARIANT, BlockStone.EnumType.DIORITE_SMOOTH);
            Assertions.assertTrue(configList.blockStateMatchers.get(Blocks.STONE).get(0).apply(targetB));

            final IBlockState target2 = Blocks.STONE.getDefaultState().withProperty(BlockStone.VARIANT, BlockStone.EnumType.GRANITE);
            Assertions.assertFalse(configList.blockStateMatchers.get(Blocks.STONE).get(0).apply(target2));
        }

        @Test
        void singleStateFuzzy_startsWithName() {
            final BlockStateConfigListOld configList = new BlockStateConfigListOld("test", null);
            Assertions.assertTrue(configList.handleBlockState("minecraft:stone[variant:smooth~]"));

            // Would expect 1 item to be present and contain a mather that fits out block
            Assertions.assertEquals(1, configList.blockStateMatchers.size());
            Assertions.assertTrue(configList.blockStateMatchers.containsKey(Blocks.STONE));
            Assertions.assertEquals(1, configList.blockStateMatchers.get(Blocks.STONE).size());

            final IBlockState targetA = Blocks.STONE.getDefaultState().withProperty(BlockStone.VARIANT, BlockStone.EnumType.GRANITE_SMOOTH);
            Assertions.assertTrue(configList.blockStateMatchers.get(Blocks.STONE).get(0).apply(targetA));
            final IBlockState targetB = Blocks.STONE.getDefaultState().withProperty(BlockStone.VARIANT, BlockStone.EnumType.DIORITE_SMOOTH);
            Assertions.assertTrue(configList.blockStateMatchers.get(Blocks.STONE).get(0).apply(targetB));

            final IBlockState target2 = Blocks.STONE.getDefaultState().withProperty(BlockStone.VARIANT, BlockStone.EnumType.GRANITE);
            Assertions.assertFalse(configList.blockStateMatchers.get(Blocks.STONE).get(0).apply(target2));
        }
    }

    @Test
    void fullTest_arrayOfStates() {
        final BlockStateConfigListOld configList = new BlockStateConfigListOld("test", (config) -> {
            final String[] entries = new String[]{
                "minecraft:stone",
                "minecraft:~ore",
                "minecraft:iron~",
                "minecraft:concrete_powder[color:lime]",
                "minecraft:concrete_powder[color:~blue]"
            };

            config.loadBlockStates(entries);
        });
        configList.reload();

        // Expect temp to clear and to be locked
        Assertions.assertEquals(0, configList.fuzzyBlockChecks.size());
        Assertions.assertTrue(configList.isLocked());

        // Expect states to be loaded
        Assertions.assertEquals(0, configList.blockStates.size());

        Assertions.assertEquals(14, configList.blocks.size());
        List<Block> blockList = Lists.newArrayList(
            Blocks.IRON_BLOCK, Blocks.LIT_REDSTONE_ORE, Blocks.IRON_TRAPDOOR, Blocks.REDSTONE_ORE,
            Blocks.COAL_ORE, Blocks.IRON_BARS, Blocks.IRON_ORE, Blocks.EMERALD_ORE, Blocks.QUARTZ_ORE,
            Blocks.GOLD_ORE, Blocks.LAPIS_ORE, Blocks.STONE, Blocks.IRON_DOOR, Blocks.DIAMOND_ORE);
        blockList.forEach((block) -> {
            Assertions.assertTrue(configList.blocks.contains(block));
        });

        Assertions.assertEquals(1, configList.blockStateMatchers.size());
        //TODO validate we added two fuzzy matchers and they work as expected
    }
}