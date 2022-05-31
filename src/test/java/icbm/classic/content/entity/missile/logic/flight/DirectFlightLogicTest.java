package icbm.classic.content.entity.missile.logic.flight;

import com.builtbroken.mc.testing.junit.TestManager;
import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.content.entity.missile.logic.reg.MissileFlightLogicRegistry;
import icbm.classic.content.entity.missile.targeting.BasicTargetData;
import icbm.classic.content.entity.missile.targeting.reg.MissileTargetRegistry;
import net.minecraft.world.World;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

class DirectFlightLogicTest
{
    static TestManager testManager = new TestManager("missile", Assertions::fail);

    final World world = testManager.getWorld();

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