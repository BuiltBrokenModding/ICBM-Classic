package icbm.classic.content.entity;

import com.builtbroken.mc.testing.junit.TestManager;
import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.caps.IExplosive;
import icbm.classic.lib.capability.ex.CapabilityExplosiveEntity;
import net.minecraftforge.common.capabilities.Capability;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * Created by Dark(DarkGuardsman, Robert) on 3/28/2020.
 */
public class EntityGrenadeTest
{
    private static TestManager testManager = new TestManager("EntityGrenade", Assertions::fail);


    @BeforeAll
    static void setup()
    {
        ICBMClassicAPI.EXPLOSIVE_CAPABILITY = (Capability<IExplosive>) Mockito.mock(Capability.class);
    }

    @AfterEach
    public void cleanupBetweenTests()
    {
        testManager.cleanupBetweenTests();
    }

    @AfterAll
    public static void tearDown()
    {
        testManager.tearDownTest();
        ICBMClassicAPI.EXPLOSIVE_CAPABILITY = null;
    }

    @Test
    void hasCapability_explosiveCap()
    {
        final EntityGrenade entityGrenade = new EntityGrenade(testManager.getWorld());
        Assertions.assertTrue(entityGrenade.hasCapability(ICBMClassicAPI.EXPLOSIVE_CAPABILITY, null));
    }

    @Test
    void getCapability_explosiveCap()
    {
        final EntityGrenade entityGrenade = new EntityGrenade(testManager.getWorld());

        //Mock cast as mockito is odd
        Mockito.when(ICBMClassicAPI.EXPLOSIVE_CAPABILITY.cast(entityGrenade.explosive)).thenReturn(entityGrenade.explosive);

        final IExplosive capability = entityGrenade.getCapability(ICBMClassicAPI.EXPLOSIVE_CAPABILITY, null);

        //Should return a capability for an entity
        Assertions.assertTrue(capability instanceof CapabilityExplosiveEntity, "Should have an explosive entity cap");
        final CapabilityExplosiveEntity cap = (CapabilityExplosiveEntity)capability;

        //Should contain the entity in question
        Assertions.assertSame(cap.entity, entityGrenade);
    }
}
