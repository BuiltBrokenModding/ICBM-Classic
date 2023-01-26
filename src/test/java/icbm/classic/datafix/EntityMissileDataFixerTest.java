package icbm.classic.datafix;

import com.adelean.inject.resources.junit.jupiter.GivenJsonResource;
import com.adelean.inject.resources.junit.jupiter.TestWithResources;
import icbm.classic.ICBMClassic;
import icbm.classic.TestBase;
import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.caps.IExplosive;
import icbm.classic.content.items.ItemGrenade;
import icbm.classic.content.items.ItemMissile;
import icbm.classic.content.reg.ItemReg;
import icbm.classic.lib.capability.ex.CapabilityExplosive;
import net.minecraft.init.Bootstrap;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@TestWithResources
public class EntityMissileDataFixerTest extends TestBase {

    @GivenJsonResource("data/saves/4.0.0/entity_missile_sonic.json")
    NBTTagCompound version400save;

    @GivenJsonResource("data/saves/4.2.0/entity_missile_sonic.json")
    NBTTagCompound version420save;

    final EntityMissileDataFixer dataFixer = new EntityMissileDataFixer();

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
        ForgeRegistries.ITEMS.register(ItemReg.itemMissile = new ItemMissile());
    }

    @Test
    @DisplayName("Loads from old version 4.0.0 save file")
    void loadFromVersion4() {

        // Check that we have saves
        Assertions.assertNotNull(version400save);
        Assertions.assertNotNull(version420save);

        // Modify expected to ignore fields we don't convert but a normal save would still have
        DataFixerHelpers.removeNestedTag(version420save, "missile", "flight", "data", "calculated");
        DataFixerHelpers.removeNestedTag(version420save, "missile", "flight", "data", "timers", "climb_height");
        DataFixerHelpers.removeNestedTag(version420save, "missile", "source", "data", "dimension");

        final NBTTagCompound updatedSave = dataFixer.fixTagCompound(version400save);

        assertTags(version420save, updatedSave);
    }
}
