package icbm.classic.content.missile.logic.flight;

import com.builtbroken.mc.testing.junit.TestManager;
import icbm.classic.world.missile.logic.flight.DeadFlightLogic;
import net.minecraft.world.entity.projectile.EntityArrow;
import net.minecraft.world.entity.projectile.EntityTippedArrow;
import net.minecraft.world.level.Level;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class DeadFlightLogicTest
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
    void shouldDecreaseMotion_start_false()
    {
        final DeadFlightLogic deadFlightLogic = new DeadFlightLogic(1000);

        EntityArrow entityArrow = new EntityTippedArrow(world);
        entityArrow.ticksExisted = 0;

        Assertions.assertFalse(deadFlightLogic.shouldDecreaseMotion(entityArrow));
    }

    @Test
    void shouldDecreaseMotion_nearDead_false()
    {
        final DeadFlightLogic deadFlightLogic = new DeadFlightLogic(1000);

        EntityArrow entityArrow = new EntityTippedArrow(world);
        entityArrow.ticksExisted = 999;

        Assertions.assertFalse(deadFlightLogic.shouldDecreaseMotion(entityArrow));
    }

    @Test
    void shouldDecreaseMotion_atDead_false()
    {
        final DeadFlightLogic deadFlightLogic = new DeadFlightLogic(1000);

        EntityArrow entityArrow = new EntityTippedArrow(world);
        entityArrow.ticksExisted = 1000;

        Assertions.assertFalse(deadFlightLogic.shouldDecreaseMotion(entityArrow));
    }

    @Test
    void shouldDecreaseMotion_pastDead_true()
    {
        final DeadFlightLogic deadFlightLogic = new DeadFlightLogic(0);

        EntityArrow entityArrow = new EntityTippedArrow(world);

        Assertions.assertTrue(deadFlightLogic.shouldDecreaseMotion(entityArrow));
    }

    @Test
    void testEquals_sameFuel_true()
    {
        final DeadFlightLogic deadFlightLogicA = new DeadFlightLogic(1000);
        final DeadFlightLogic deadFlightLogicB = new DeadFlightLogic(1000);

        Assertions.assertEquals(deadFlightLogicA, deadFlightLogicB);
    }

    @Test
    void testEquals_diffFuel_false()
    {
        final DeadFlightLogic deadFlightLogicA = new DeadFlightLogic(1000);
        final DeadFlightLogic deadFlightLogicB = new DeadFlightLogic(1001);

        Assertions.assertNotEquals(deadFlightLogicA, deadFlightLogicB);
    }
}