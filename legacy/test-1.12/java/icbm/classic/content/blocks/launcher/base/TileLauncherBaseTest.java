package icbm.classic.content.blocks.launcher.base;

import com.adelean.inject.resources.junit.jupiter.GivenJsonResource;
import com.adelean.inject.resources.junit.jupiter.TestWithResources;
import icbm.classic.ICBMClassic;
import icbm.classic.TestBase;
import icbm.classic.content.items.ItemMissile;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@TestWithResources
public class TileLauncherBaseTest extends TestBase {

    @GivenJsonResource("data/saves/4.0.0/tileEntity_launcherbase.json")
    NBTTagCompound version400save;

    @GivenJsonResource("data/saves/4.2.0/tileEntity_launcherbase.json")
    NBTTagCompound version420save;

    private static Item item;

    @BeforeAll
    public static void beforeAllTests()
    {
        // Register block for placement
        ForgeRegistries.ITEMS.register(item = new ItemMissile().setName("explosive_missile").setCreativeTab(ICBMClassic.CREATIVE_TAB));
    }

    @Test
    @DisplayName("Loads from version 4.0.0 save file")
    void loadFromVersion400() {

        // Validate we have a test file
        Assertions.assertNotNull(version400save);

        // Update itemMissile registry name, mapping event changes this as start of game for us
        ((NBTTagCompound) version400save.getCompoundTag("inventory").getTagList("Items", 10).get(0)).setString("id", "icbmclassic:explosive_missile");

        // Load tile
        final TileLauncherBase launcher = new TileLauncherBase();
        launcher.readFromNBT(version400save);

        // Confirm we still have a missile in inventory
        assertExplosive(launcher.getMissileStack(), "icbmclassic:shrapnel", new NBTTagCompound());
    }

    @Test
    @DisplayName("Loads from version 4.2.0 save file")
    void loadFromVersion420() {

        // Validate we have a test file
        Assertions.assertNotNull(version400save);

        // Load tile
        final TileLauncherBase launcher = new TileLauncherBase();
        launcher.readFromNBT(version420save);

        // Confirm we still have a missile in inventory
        assertExplosive(launcher.getMissileStack(), "icbmclassic:shrapnel", new NBTTagCompound());
    }
}
