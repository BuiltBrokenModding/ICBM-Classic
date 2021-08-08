package icbm.classic.content.blast.redmatter.logic;

import com.builtbroken.mc.testing.junit.TestManager;
import icbm.classic.content.blast.redmatter.EntityRedmatter;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * Created by Robin Seifert on 8/8/2021.
 */
public class RedmatterLogicTest
{
    private static TestManager testManager = new TestManager("Redmatter", Assertions::fail);


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

    @Test
    @DisplayName("Init: Validate starting radius of 1")
    void testInit_ShouldStartWithRadiusOfOne()
    {
        //Create redmatter
        final EntityRedmatter redmatter = new EntityRedmatter(testManager.getWorld());

        final RedmatterLogic logic = redmatter.redmatterLogic;
        Assertions.assertEquals(1, logic.currentBlockDestroyRadius, "Starting size should be 1");
    }

    @Nested
    @DisplayName("DestroyBlocks: New Cycle")
    class StartNextBlockDestroyCycle
    {

        @Test
        void collectBlocksSize1()
        {

            final World world = testManager.getWorld();

            //Build world
            world.setBlockState(new BlockPos(8, 0, 8), Blocks.STONE.getDefaultState());

            //Create redmatter
            final EntityRedmatter redmatter = new EntityRedmatter(world);
            redmatter.setBlastSize(70); //Default size
            redmatter.posX = 8;
            redmatter.posY = 1;
            redmatter.posZ = 8;
            world.spawnEntity(redmatter);

            //Validate starting conditions
            final RedmatterLogic logic = redmatter.redmatterLogic;

            //Invoke cycle start directly bypassing tick() logic
            logic.startNextBlockDestroyCycle();

            //Validate our data fields
            Assertions.assertEquals(0, logic.blockDestroyedThisCycle, "No blocks should be destroyed");
            Assertions.assertEquals(1, logic.currentBlockDestroyRadius, "Should still have a size of 1");
            Assertions.assertEquals(0, logic.cyclesSinceLastBlockRemoved, "First cycle should finish with 0 cycles since blocks removed");

            //Validate that we generated a list of block target
            Assertions.assertEquals(RedmatterBlockCollectorTest.ALL_DATA.size(), logic.rayTraceTargets.size());
            logic.rayTraceTargets.iterator().forEachRemaining((blockPos) -> {
                Assertions.assertTrue(RedmatterBlockCollectorTest.ALL_DATA.contains(blockPos));
            });
        }
    }

    @Nested
    @DisplayName("DestroyBlocks: Raytrace target")
    class RayTraceTowardsBlock
    {
        @Test
        void raytraceTarget_verifyMath()
        {
            final World world = Mockito.spy(testManager.getWorld());
            final EntityRedmatter redmatter = new EntityRedmatter(world);
            final RedmatterLogic logic = redmatter.redmatterLogic;

            //Invoke method
            logic.rayTraceTowardsBlock(new Vec3d(0.5, 0.5, 0.5), new BlockPos(3, 1, 2));

            //Validate we called ray trace with the correct position data
            Mockito.verify(world).rayTraceBlocks(
                    Mockito.argThat(arg -> arg.x - 0.5 <= 0.0000 && arg.y - 0.5 <= 0.0000 && arg.z - 0.5 <= 0.0000),
                    Mockito.argThat(arg -> arg.x - 3.5 <= 0.0000 && arg.y - 1.5 <= 0.0000 && arg.z - 2.5 <= 0.0000),
                    Mockito.booleanThat((d) -> d),
                    Mockito.booleanThat((d) -> !d),
                    Mockito.booleanThat((d) -> !d)
            );
        }

        @Test
        void raytraceTarget_verifyHit()
        {
            final World world = testManager.getWorld();
            final EntityRedmatter redmatter = new EntityRedmatter(world);
            final RedmatterLogic logic = Mockito.spy(redmatter.redmatterLogic);
            world.setBlockState(new BlockPos(2, 0, 0), Blocks.STONE.getDefaultState());

            //Invoke method
            logic.rayTraceTowardsBlock(new Vec3d(0.5, 0.5, 0.5), new BlockPos(3, 0, 0));

            //Validate we passed the ray hit to the next method
            Mockito.verify(logic).processNextBlock(Mockito.argThat(pos -> pos.getX() == 2 && pos.getY() == 0 && pos.getZ() == 0));
        }

        @Test
        void raytraceTarget_verifyRemoveBlock()
        {
            final World world = testManager.getWorld();
            final EntityRedmatter redmatter = new EntityRedmatter(world);
            final RedmatterLogic logic = redmatter.redmatterLogic;
            logic.currentBlockDestroyRadius = 5;
            world.setBlockState(new BlockPos(2, 0, 0), Blocks.STONE.getDefaultState());

            //Invoke method
            logic.rayTraceTowardsBlock(new Vec3d(0.5, 0.5, 0.5), new BlockPos(3, 0, 0));

            //Validate we passed the ray hit to the next method
            Assertions.assertEquals(Blocks.AIR, world.getBlockState(new BlockPos(2, 0, 0)).getBlock());
        }
    }
}
