package icbm.classic.lib.capability.ex;

import com.builtbroken.mc.testing.junit.TestManager;
import net.minecraft.world.entity.monster.EntityZombie;
import net.minecraft.init.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.junit.jupiter.api.*;

class CapabilityExplosiveEntityTest
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
    void testEquals_sameItem_true()
    {
        final CapabilityExplosiveEntity capA = new CapabilityExplosiveEntity(new EntityZombie(world));
        capA.setStack(new ItemStack(Items.STONE_AXE));

        final CapabilityExplosiveEntity capB = new CapabilityExplosiveEntity(new EntityZombie(world));
        capB.setStack(new ItemStack(Items.STONE_AXE));

        Assertions.assertEquals(capA, capB);
    }

    @Test
    void testEquals_air_true()
    {
        final CapabilityExplosiveEntity capA = new CapabilityExplosiveEntity(new EntityZombie(world));
        final CapabilityExplosiveEntity capB = new CapabilityExplosiveEntity(new EntityZombie(world));
        Assertions.assertEquals(capA, capB);
    }

    @Test
    void testEquals_diffItem_false()
    {
        final CapabilityExplosiveEntity capA = new CapabilityExplosiveEntity(new EntityZombie(world));
        capA.setStack(new ItemStack(Items.STONE_AXE));

        final CapabilityExplosiveEntity capB = new CapabilityExplosiveEntity(new EntityZombie(world));
        capB.setStack(new ItemStack(Items.STONE_PICKAXE));

        Assertions.assertNotEquals(capA, capB);
    }
}