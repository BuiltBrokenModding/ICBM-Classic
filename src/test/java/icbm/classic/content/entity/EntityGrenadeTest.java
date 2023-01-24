package icbm.classic.content.entity;

import com.adelean.inject.resources.junit.jupiter.GivenJsonResource;
import com.adelean.inject.resources.junit.jupiter.TestWithResources;
import com.builtbroken.mc.testing.junit.TestManager;
import icbm.classic.ICBMClassic;
import icbm.classic.TestBase;
import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.caps.IExplosive;
import icbm.classic.content.items.ItemBombCart;
import icbm.classic.content.items.ItemGrenade;
import icbm.classic.lib.capability.ex.CapabilityExplosive;
import icbm.classic.lib.capability.ex.CapabilityExplosiveEntity;
import net.minecraft.init.Bootstrap;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;

/**
 * Created by Dark(DarkGuardsman, Robert) on 3/28/2020.
 */
@TestWithResources
public class EntityGrenadeTest extends TestBase
{
    @GivenJsonResource("data/saves/4.0.0/entity_grenade_sharpnel.json")
    NBTTagCompound version4save;

    @BeforeAll
    public static void beforeAllTests()
    {
        // Start vanilla
        Bootstrap.register();

        // Setup explosive registry
        ICBMClassicAPI.EXPLOSIVE_CAPABILITY = getCapOrCreate(IExplosive.class, CapabilityExplosive::register);
        ICBMClassic.INSTANCE = new ICBMClassic();
        ICBMClassic.INSTANCE.handleExRegistry(null);

        // Register block for placement
        ForgeRegistries.ITEMS.register(new ItemGrenade().setName("grenade"));
    }

    @Test
    @DisplayName("Loads from old version 4.0.0 save file")
    void loadFromVersion4() {
        final World world = testManager.getWorld();
        final EntityGrenade grenade = new EntityGrenade(world);

        // Validate we have a test file
        Assertions.assertNotNull(version4save);

        // Load entity custom save
        grenade.readEntityFromNBT(version4save);

        // Validate we have an explosive of the correct type
        assertExplosive(grenade.getCapability(ICBMClassicAPI.EXPLOSIVE_CAPABILITY, null), "icbmclassic:shrapnel", new NBTTagCompound());
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

        final IExplosive capability = entityGrenade.getCapability(ICBMClassicAPI.EXPLOSIVE_CAPABILITY, null);

        //Should return a capability for an entity
        Assertions.assertTrue(capability instanceof CapabilityExplosiveEntity, "Should have an explosive entity cap");
        final CapabilityExplosiveEntity cap = (CapabilityExplosiveEntity)capability;

        //Should contain the entity in question
        Assertions.assertSame(cap.entity, entityGrenade);
    }
}
