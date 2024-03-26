package icbm.classic.content.blocks.explosive;

import com.adelean.inject.resources.junit.jupiter.GivenJsonResource;
import com.adelean.inject.resources.junit.jupiter.TestWithResources;
import icbm.classic.TestBase;
import icbm.classic.world.block.explosive.ExplosiveBlock;
import icbm.classic.world.block.explosive.ItemBlockExplosive;
import net.minecraft.world.level.block.Block;
import net.minecraft.init.Bootstrap;
import net.minecraft.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@TestWithResources
class ItemExplosiveBlockTest extends TestBase {

    @GivenJsonResource("data/saves/4.0.0/itemstack_ContagiousExplosives.json")
    CompoundTag version4save;

    private static Item item;

    public ItemExplosiveBlockTest() {
        super(null);
    }

    @BeforeAll
    public static void beforeAllTests()
    {
        // Register block for placement
        final Block block = new ExplosiveBlock();
        ForgeRegistries.BLOCKS.register(block);
        ForgeRegistries.ITEMS.register(item = new ItemBlockExplosive(block).setRegistryName(block.getRegistryName()));
    }

    @Test
    @DisplayName("Loads from old version 4.0.0 save file")
    void loadFromVersion4() {

        // Validate we have a test file
        Assertions.assertNotNull(version4save);

        // Load stack
        final ItemStack stack = new ItemStack(version4save);

        // Create compare stack
        final ItemStack expected = new ItemStack(item, 1, 9);

        // Compare stacks
        Assertions.assertTrue(ItemStack.areItemsEqual(expected, stack));

        // Confirm capability returns the correct explosive
        assertExplosive(stack, "icbmclassic:contagious", new CompoundTag());
    }
}
