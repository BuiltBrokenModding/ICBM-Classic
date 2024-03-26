package icbm.classic.content.blocks.launcher.base;

import com.adelean.inject.resources.junit.jupiter.GivenJsonResource;
import com.adelean.inject.resources.junit.jupiter.TestWithResources;
import icbm.classic.ICBMClassic;
import icbm.classic.TestBase;
import icbm.classic.world.block.launcher.base.LauncherBaseBlockEntity;
import icbm.classic.world.item.MissileItem;
import net.minecraft.item.Item;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@TestWithResources
public class LauncherBaseBlockEntityTest extends TestBase {

    @GivenJsonResource("data/saves/4.0.0/tileEntity_launcherbase.json")
    CompoundTag version400save;

    @GivenJsonResource("data/saves/4.2.0/tileEntity_launcherbase.json")
    CompoundTag version420save;

    private static Item item;

    @BeforeAll
    public static void beforeAllTests()
    {
        // Register block for placement
        ForgeRegistries.ITEMS.register(item = new MissileItem().setName("explosive_missile").setCreativeTab(ICBMClassic.CREATIVE_TAB));
    }

    @Test
    @DisplayName("Loads from version 4.0.0 save file")
    void loadFromVersion400() {

        // Validate we have a test file
        Assertions.assertNotNull(version400save);

        // Update itemMissile registry name, mapping event changes this as start of game for us
        ((CompoundTag) version400save.getCompound("inventory").getTagList("Items", 10).get(0)).putString("id", "icbmclassic:explosive_missile");

        // Load tile
        final LauncherBaseBlockEntity launcher = new LauncherBaseBlockEntity();
        launcher.readFromNBT(version400save);

        // Confirm we still have a missile in inventory
        assertExplosive(launcher.getMissileStack(), "icbmclassic:shrapnel", new CompoundTag());
    }

    @Test
    @DisplayName("Loads from version 4.2.0 save file")
    void loadFromVersion420() {

        // Validate we have a test file
        Assertions.assertNotNull(version400save);

        // Load tile
        final LauncherBaseBlockEntity launcher = new LauncherBaseBlockEntity();
        launcher.readFromNBT(version420save);

        // Confirm we still have a missile in inventory
        assertExplosive(launcher.getMissileStack(), "icbmclassic:shrapnel", new CompoundTag());
    }
}
