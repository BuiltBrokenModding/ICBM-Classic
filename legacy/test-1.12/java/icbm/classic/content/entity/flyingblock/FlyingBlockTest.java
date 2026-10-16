package icbm.classic.content.entity.flyingblock;

import com.builtbroken.mc.testing.junit.TestManager;
import com.google.common.collect.Lists;
import icbm.classic.config.ConfigFlyingBlocks;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.init.Bootstrap;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.junit.jupiter.api.*;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Function;

class FlyingBlockTest {

    private static TestManager testManager = new TestManager("CommandBlastListTest", Assertions::fail);

    @AfterEach
    public void cleanupBetweenTests()
    {
        testManager.cleanupBetweenTests();
    }

    @AfterAll
    public static void tearDown()
    {
        testManager.tearDownTest();
    }
    @BeforeAll
    static void beforeAll() {
        Bootstrap.register();
    }
    @Test
    void loadFromConfig() {
        ConfigFlyingBlocks.banAllow.blockStates = new String[] {"minecraft:iron_~", "minecraft:~_ore", "minecraft:stone"};
        FlyingBlock.loadFromConfig();

        Assertions.assertEquals(Lists.newArrayList(
            "minecraft:stone",
            "minecraft:gold_ore",
            "minecraft:iron_ore",
            "minecraft:coal_ore",
            "minecraft:lapis_ore",
            "minecraft:iron_block",
            "minecraft:diamond_ore",
            "minecraft:iron_door",
            "minecraft:redstone_ore",
            "minecraft:lit_redstone_ore",
            "minecraft:iron_bars",
            "minecraft:emerald_ore",
            "minecraft:quartz_ore",
            "minecraft:iron_trapdoor"
        ), FlyingBlock.banAllowList.dumpBlocksContained());
    }

    @Test
    @DisplayName("Confirm we can spawn a flying block if allowed")
    void spawnFlyingBlock_allowedToSpawn() {
        final World world = testManager.getWorld(0);
        final BlockPos pos = new BlockPos(10, 10, 10);

        world.setBlockState(pos, Blocks.STONE.getDefaultState());


        final boolean result = FlyingBlock.spawnFlyingBlock(world, pos,  null, null);
        Assertions.assertTrue(result);

        assertMobCountInChunk(world, pos, (entity) -> entity instanceof EntityFlyingBlock, 1);
        assertMobCountInChunk(world, pos, (entity) -> !(entity instanceof EntityFlyingBlock), 1);
        // TODO optimize to return map<class, count>
    }

    @Test
    @DisplayName("Confirm we don't spawn a flying block if disallowed")
    void spawnFlyingBlock_disallowedToSpawn() {
        final World world = testManager.getWorld(0);

        FlyingBlock.banAllowList.loadBlockStates("minecraft:dirt");

        final BlockPos pos = new BlockPos(10, 10, 10);
        world.setBlockState(pos, Blocks.DIRT.getDefaultState());

        final boolean result = FlyingBlock.spawnFlyingBlock(world, pos, null, null);
        Assertions.assertFalse(result);

        assertMobCountInChunk(world, pos, Objects::nonNull, 0);
    }

    void assertMobCountInChunk(World world, BlockPos chunkBlockPos, Function<Entity, Boolean> matcher, int count) {
      Assertions.assertEquals(count, Arrays.stream(world.getChunkFromBlockCoords(chunkBlockPos).getEntityLists())
            .mapToInt(entities -> (int)entities.stream()
                .filter(matcher::apply).count()).sum());
    }
}