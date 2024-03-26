package icbm.classic.content.entity;

import com.adelean.inject.resources.junit.jupiter.GivenJsonResource;
import com.adelean.inject.resources.junit.jupiter.TestWithResources;
import icbm.classic.TestBase;
import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.caps.IExplosive;
import icbm.classic.world.entity.GrenadeEntity;
import icbm.classic.world.item.GrenadeItem;
import icbm.classic.lib.capability.ex.CapabilityExplosiveEntity;
import net.minecraft.init.Bootstrap;
import net.minecraft.item.Item;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import org.junit.jupiter.api.*;

/**
 * Created by Dark(DarkGuardsman, Robert) on 3/28/2020.
 */
@TestWithResources
public class EntityGrenadeTest extends TestBase
{
    @GivenJsonResource("data/saves/4.0.0/entity_grenade_sharpnel.json")
    CompoundTag version4save;

    @BeforeAll
    public static void beforeAllTests()
    {
        // Register block for placement
        ForgeRegistries.ITEMS.register(new GrenadeItem().setName("grenade"));
    }

    @Test
    @DisplayName("Loads from old version 4.0.0 save file")
    void loadFromVersion4() {
        final Level level = testManager.getLevel();
        final GrenadeEntity grenade = new GrenadeEntity(world);

        // Validate we have a test file
        Assertions.assertNotNull(version4save);

        // Load entity custom save
        grenade.readEntityFromNBT(version4save);

        // Validate we have an explosive of the correct type
        assertExplosive(grenade.getCapability(ICBMClassicAPI.EXPLOSIVE_CAPABILITY, null), "icbmclassic:shrapnel", new CompoundTag());
    }

    @Test
    void hasCapability_explosiveCap()
    {
        final GrenadeEntity entityGrenade = new GrenadeEntity(testManager.getLevel());
        Assertions.assertTrue(entityGrenade.hasCapability(ICBMClassicAPI.EXPLOSIVE_CAPABILITY, null));
    }

    @Test
    void getCapability_explosiveCap()
    {
        final GrenadeEntity entityGrenade = new GrenadeEntity(testManager.getLevel());

        final IExplosive capability = entityGrenade.getCapability(ICBMClassicAPI.EXPLOSIVE_CAPABILITY, null);

        //Should return a capability for an entity
        Assertions.assertTrue(capability instanceof CapabilityExplosiveEntity, "Should have an explosive entity cap");
        final CapabilityExplosiveEntity cap = (CapabilityExplosiveEntity)capability;

        //Should contain the entity in question
        Assertions.assertSame(cap.entity, entityGrenade);
    }
}
