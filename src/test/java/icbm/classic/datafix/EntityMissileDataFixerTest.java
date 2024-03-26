package icbm.classic.datafix;

import com.adelean.inject.resources.junit.jupiter.GivenJsonResource;
import com.adelean.inject.resources.junit.jupiter.TestWithResources;
import icbm.classic.ICBMClassic;
import icbm.classic.TestBase;
import icbm.classic.world.item.MissileItem;
import icbm.classic.world.IcbmItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@TestWithResources
public class EntityMissileDataFixerTest extends TestBase {

    @GivenJsonResource("data/saves/4.0.0/entity_missile_ballistic.json")
    CompoundTag ballistic400save;

    @GivenJsonResource("data/saves/4.0.0/entity_missile_rpg.json")
    CompoundTag rpg400save;

    @GivenJsonResource("data/saves/4.0.0/entity_missile_cruise.json")
    CompoundTag cruise400save;

    @GivenJsonResource("data/saves/fixer/entity_missile_ballistic.json")
    CompoundTag ballistic420save;

    @GivenJsonResource("data/saves/fixer/entity_missile_rpg.json")
    CompoundTag rpg420save;

    @GivenJsonResource("data/saves/fixer/entity_missile_cruise.json")
    CompoundTag cruise420save;



    @BeforeAll
    public static void beforeAllTests()
    {
        // Register block for placement
        ForgeRegistries.ITEMS.register(IcbmItems.itemExplosiveMissile = new MissileItem().setName("explosive_missile").setCreativeTab(ICBMClassic.CREATIVE_TAB));
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

        final CompoundTag updatedSave = EntityMissileDataFixer.INSTANCE.fixTagCompound(ballistic400save);

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

        final CompoundTag updatedSave = EntityMissileDataFixer.INSTANCE.fixTagCompound(rpg400save);

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

        final CompoundTag updatedSave = EntityMissileDataFixer.INSTANCE.fixTagCompound(cruise400save);

        assertTags(cruise420save, updatedSave);
    }
}
