package icbm.classic.content.missile.logic.flight;

import com.builtbroken.mc.testing.junit.TestManager;
import icbm.classic.world.missile.logic.flight.DirectFlightLogic;
import icbm.classic.world.missile.logic.targeting.BasicTargetData;
import net.minecraft.world.level.Level;
import org.junit.jupiter.api.*;

class DirectFlightLogicTest
{
    static TestManager testManager = new TestManager("missile", Assertions::fail);

    final Level level = testManager.getLevel();

    @AfterAll
    public static void afterAllTests()
    {
        testManager.tearDownTest();
    }

    @AfterEach
    public void afterEachTest()
    {
        testManager.cleanupBetweenTests();
    }

    @Test
    void testEquals_sameMotion_true()
    {
        final DirectFlightLogic objA = new DirectFlightLogic();
        objA.calculateFlightPath(world, 100, 100, 100, new BasicTargetData(200, 110, 120));

        final DirectFlightLogic objB = new DirectFlightLogic();
        objB.calculateFlightPath(world, 100, 100, 100, new BasicTargetData(200, 110, 120));

        Assertions.assertEquals(objA, objB);
    }

    @Test
    void testEquals_diffMotion_false()
    {
        final DirectFlightLogic objA = new DirectFlightLogic();
        objA.calculateFlightPath(world, 100, 100, 100, new BasicTargetData(200, 110, 120));

        final DirectFlightLogic objB = new DirectFlightLogic();
        objB.calculateFlightPath(world, 200, 100, 100, new BasicTargetData(200, 110, 120));

        Assertions.assertNotEquals(objA, objB);
    }
}