package icbm.classic.content.items;

import com.adelean.inject.resources.junit.jupiter.GivenJsonResource;
import com.adelean.inject.resources.junit.jupiter.TestWithResources;
import icbm.classic.ICBMClassic;
import icbm.classic.TestBase;
import icbm.classic.world.item.MissileItem;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.eq;

/**
 * Created by Dark(DarkGuardsman, Robin) on 12/25/2019.
 */
@TestWithResources
public class ItemMissileTest extends TestBase
{
    @GivenJsonResource("data/saves/4.0.0/itemstack_missile.json")
    CompoundTag version400save;

    @GivenJsonResource("data/saves/4.2.0/itemstack_missile.json")
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

        // Update registry name, this is done by remapping event
        version400save.putString("id", "icbmclassic:explosive_missile");

        // Load stack
        final ItemStack stack = new ItemStack(version400save);

        // Create compare stack
        final ItemStack expected = new ItemStack(item, 1, 1);

        // Compare stacks
        Assertions.assertTrue(ItemStack.areItemsEqual(expected, stack));

        // Confirm capability returns the correct explosive
        assertExplosive(stack, "icbmclassic:shrapnel", new CompoundTag());
    }

    @Test
    @DisplayName("Loads from version 4.2.0 save file")
    void loadFromVersion420() {

        // Validate we have a test file
        Assertions.assertNotNull(version420save);

        // Load stack
        final ItemStack stack = new ItemStack(version420save);

        // Create compare stack
        final ItemStack expected = new ItemStack(item, 1, 1);

        // Compare stacks
        Assertions.assertTrue(ItemStack.areItemsEqual(expected, stack));

        // Confirm capability returns the correct explosive
        assertExplosive(stack, "icbmclassic:shrapnel", new CompoundTag());
    }
}
