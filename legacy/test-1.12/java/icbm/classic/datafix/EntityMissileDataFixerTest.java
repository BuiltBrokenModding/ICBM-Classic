package icbm.classic.datafix;

import com.adelean.inject.resources.junit.jupiter.GivenBinaryResource;
import com.adelean.inject.resources.junit.jupiter.GivenJsonResource;
import com.adelean.inject.resources.junit.jupiter.TestWithResources;
import com.lunarshark.nbttool.mod.NBTTool;
import com.lunarshark.nbttool.utils.SaveToJson;
import icbm.classic.ICBMClassic;
import icbm.classic.TestBase;
import icbm.classic.content.items.ItemMissile;
import icbm.classic.content.reg.ItemReg;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.*;

@TestWithResources
public class EntityMissileDataFixerTest extends TestBase {

    @GivenJsonResource("data/saves/4.0.0/entity_missile_ballistic.json")
    NBTTagCompound ballistic400save;

    @GivenJsonResource("data/saves/4.0.0/entity_missile_rpg.json")
    NBTTagCompound rpg400save;

    @GivenJsonResource("data/saves/4.0.0/entity_missile_cruise.json")
    NBTTagCompound cruise400save;

    @GivenJsonResource("data/saves/fixer/entity_missile_ballistic.json")
    NBTTagCompound ballistic420save;

    @GivenJsonResource("data/saves/fixer/entity_missile_rpg.json")
    NBTTagCompound rpg420save;

    @GivenJsonResource("data/saves/fixer/entity_missile_cruise.json")
    NBTTagCompound cruise420save;



    @BeforeAll
    public static void beforeAllTests()
    {
        // Register block for placement
        ForgeRegistries.ITEMS.register(ItemReg.itemExplosiveMissile = new ItemMissile().setName("explosive_missile").setCreativeTab(ICBMClassic.CREATIVE_TAB));
    }

    @Test
    @DisplayName("Updates v4.0.0 ballastic missile save")
    void loadFromVersion4_ballistic() {

        // Check that we have saves
        Assertions.assertNotNull(ballistic400save);
        Assertions.assertNotNull(ballistic420save);

        // Modify expected to ignore fields we don't convert but a normal save would still have
        DataFixerHelpers.removeNestedTag(ballistic420save, "missile", "flight", "data", "calculated");
        DataFixerHelpers.removeNestedTag(ballistic420save, "missile", "flight", "data", "timers", "climb_height");
        DataFixerHelpers.removeNestedTag(ballistic420save, "missile", "source", "data", "dimension");

        final NBTTagCompound updatedSave = EntityMissileDataFixer.INSTANCE.fixTagCompound(ballistic400save);

        assertTags(ballistic420save, updatedSave);
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

        final NBTTagCompound updatedSave = EntityMissileDataFixer.INSTANCE.fixTagCompound(rpg400save);

        assertTags(rpg420save, updatedSave);
    }

    @Test
    @DisplayName("Updates v4.0.0 cruise missile save")
    void loadFromVersion4_cruise() {

        // Check that we have saves
        Assertions.assertNotNull(cruise400save);
        Assertions.assertNotNull(cruise420save);

        // Modify expected to ignore fields we don't convert but a normal save would still have
        DataFixerHelpers.removeNestedTag(cruise420save, "missile", "source", "data", "dimension");
        DataFixerHelpers.removeNestedTag(cruise420save, "health");
        DataFixerHelpers.removeNestedTag(cruise420save, "missile", "source", "data", "entity");

        final NBTTagCompound updatedSave = EntityMissileDataFixer.INSTANCE.fixTagCompound(cruise400save);

        assertTags(cruise420save, updatedSave);
    }
}
