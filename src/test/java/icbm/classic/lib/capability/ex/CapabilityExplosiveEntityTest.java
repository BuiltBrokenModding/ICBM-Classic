package icbm.classic.lib.capability.ex;

import com.builtbroken.mc.testing.junit.TestManager;
import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.content.entity.missile.logic.flight.DeadFlightLogic;
import icbm.classic.content.entity.missile.logic.reg.MissileFlightLogicRegistry;
import icbm.classic.content.entity.missile.targeting.BasicTargetData;
import icbm.classic.content.entity.missile.targeting.reg.MissileTargetRegistry;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

class CapabilityExplosiveEntityTest
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