package icbm.classic.datafix;

import com.adelean.inject.resources.junit.jupiter.GivenJsonResource;
import com.adelean.inject.resources.junit.jupiter.TestWithResources;
import icbm.classic.ICBMClassic;
import icbm.classic.TestBase;
import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.caps.IExplosive;
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
    NBTTagCompound ballastic400save;

    @GivenJsonResource("data/saves/4.2.0/entity_missile_sonic.json")
    NBTTagCompound ballastic420save;

    @GivenJsonResource("data/saves/4.0.0/entity_missile_rpg.json")
    NBTTagCompound rpg400save;

    @GivenJsonResource("data/saves/4.2.0/entity_missile_rpg.json")
    NBTTagCompound rpg420save;

    final EntityMissileDataFixer dataFixer = new EntityMissileDataFixer();

    @BeforeAll
    public static void beforeAllTests()
    {
        // Register block for placement
        ForgeRegistries.ITEMS.register(ItemReg.itemMissile = new ItemMissile());
    }

    @Test
    @DisplayName("Updates v4.0.0 ballastic missile save")
    void loadFromVersion4_ballistic() {

        // Check that we have saves
        Assertions.assertNotNull(ballastic400save);
        Assertions.assertNotNull(ballastic420save);

        // Modify expected to ignore fields we don't convert but a normal save would still have
        DataFixerHelpers.removeNestedTag(ballastic420save, "missile", "flight", "data", "calculated");
        DataFixerHelpers.removeNestedTag(ballastic420save, "missile", "flight", "data", "timers", "climb_height");
        DataFixerHelpers.removeNestedTag(ballastic420save, "missile", "source", "data", "dimension");

        final NBTTagCompound updatedSave = dataFixer.fixTagCompound(ballastic400save);

        assertTags(ballastic420save, updatedSave);
    }

    @Test
    @DisplayName("Updates v4.0.0 rpg missile save")
    void loadFromVersion4_rpg() {

        // Check that we have saves
        Assertions.assertNotNull(rpg400save);
        Assertions.assertNotNull(rpg420save);

        // Modify expected to ignore fields we don't convert but a normal save would still have
        DataFixerHelpers.removeNestedTag(rpg420save, "missile", "source", "data", "dimension");
        DataFixerHelpers.removeNestedTag(rpg420save, "health");
        DataFixerHelpers.removeNestedTag(rpg420save, "missile", "source", "data", "entity");

        final NBTTagCompound updatedSave = dataFixer.fixTagCompound(rpg400save);

        assertTags(rpg420save, updatedSave);
    }
}
