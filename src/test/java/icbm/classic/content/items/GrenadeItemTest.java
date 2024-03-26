package icbm.classic.content.items;

import com.adelean.inject.resources.junit.jupiter.GivenJsonResource;
import com.adelean.inject.resources.junit.jupiter.TestWithResources;
import icbm.classic.ICBMClassic;
import icbm.classic.TestBase;
import icbm.classic.world.entity.GrenadeEntity;
import icbm.classic.world.item.GrenadeItem;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.init.Bootstrap;
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
import org.junit.jupiter.api.*;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Created by Dark(DarkGuardsman, Robin) on 12/25/2019.
 */
@TestWithResources
public class GrenadeItemTest extends TestBase
{
    @GivenJsonResource("data/saves/4.0.0/itemstack_Greande_shrapnel.json")
    CompoundTag version4save;

    private static Item item;

    @BeforeAll
    public static void beforeAllTests()
    {
        // Register block for placement
        ForgeRegistries.ITEMS.register(item = new GrenadeItem().setName("grenade").setCreativeTab(ICBMClassic.CREATIVE_TAB));
    }

    @Test
    void onItemRightClick_normal()
    {
        final Player player = spy(testManager.getPlayer());
        final Level level = testManager.getLevel();
        final ItemStack stack = new ItemStack(item, 1, 0);

        //Set player inventory and held
        player.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, stack);

        //Prevent normal set active logic as this will crash if run
        doNothing().when(player).setActiveHand(InteractionHand.MAIN_HAND);

        //Trigger action
        final ActionResult<ItemStack> result = item.onItemRightClick(world, player, InteractionHand.MAIN_HAND);

        //Main hand should be set
        verify(player, times(1)).setActiveHand(InteractionHand.MAIN_HAND);

        //Check result
        Assertions.assertEquals(item, result.getResult().getItem());
        Assertions.assertEquals(EnumActionResult.SUCCESS, result.getType());
    }

    @Test
    void onPlayerStoppedUsing_normal()
    {
        final Player player = testManager.getPlayer();
        final Level level = spy(testManager.getLevel());

        ItemStack stack = new ItemStack(item, 2, 0);

        //Trigger action
        item.onPlayerStoppedUsing(stack, world, player, GrenadeItem.MAX_USE_DURATION);

        //Check that we spawned the entity
        final ArgumentCaptor<Entity> entityArgumentCaptor = ArgumentCaptor.forClass(Entity.class);
        verify(world, times(1)).spawnEntity(entityArgumentCaptor.capture());

        //Validate we spawned entity with the right settings
        Entity entity = entityArgumentCaptor.getValue();
        Assertions.assertTrue(entity instanceof GrenadeEntity);
        Assertions.assertSame(player, ((GrenadeEntity)entity).getThrower());
        Assertions.assertEquals(stack.getItem(), ((GrenadeEntity)entity).explosive.toStack().getItem());

        //Check that we played a sound
        verify(world, times(1)).playSound(Mockito.isNull(), eq(player.getX()), eq(player.getY()), eq(player.getZ()), eq(SoundEvents.ENTITY_TNT_PRIMED), eq(SoundCategory.BLOCKS), Mockito.anyFloat(), Mockito.anyFloat());

        //Check that we called shrink
        Assertions.assertEquals(1, stack.getCount());
    }

    @Test
    @DisplayName("Loads from old version 4.0.0 save file")
    void loadFromVersion4() {

        // Validate we have a test file
        Assertions.assertNotNull(version4save);

        // Load stack
        final ItemStack stack = new ItemStack(version4save);

        // Create compare stack
        final ItemStack expected = new ItemStack(item, 1, 1);

        // Compare stacks
        Assertions.assertTrue(ItemStack.areItemsEqual(expected, stack));

        // Confirm capability returns the correct explosive
        assertExplosive(stack, "icbmclassic:shrapnel", new CompoundTag());
    }
}
