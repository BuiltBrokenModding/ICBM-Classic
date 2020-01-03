package icbm.classic.content.items;

import com.builtbroken.mc.testing.junit.TestManager;
import icbm.classic.content.entity.EntityGrenade;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Created by Dark(DarkGuardsman, Robert) on 12/25/2019.
 */
public class TestItemGrenade
{
    final ItemGrenade itemGrenade = new ItemGrenade();
    static TestManager testManager = new TestManager("itemGrenade", Assertions::fail);

    @AfterAll
    public static void afterAllTests()
    {
       testManager.tearDownTest();
    }

    @Test
    void onItemRightClick_normal()
    {
        final EntityPlayer player = spy(testManager.getPlayer());
        final World world = testManager.getWorld();
        final ItemStack stack = new ItemStack(itemGrenade, 1, 0);

        //Set player inventory and held
        player.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, stack);

        //Prevent normal set active logic as this will crash if run
        doNothing().when(player).setActiveHand(EnumHand.MAIN_HAND);

        //Trigger action
        final ActionResult<ItemStack> result = itemGrenade.onItemRightClick(world, player, EnumHand.MAIN_HAND);

        //Main hand should be set
        verify(player, times(1)).setActiveHand(EnumHand.MAIN_HAND);

        //Check result
        Assertions.assertEquals(itemGrenade, result.getResult().getItem());
        Assertions.assertEquals(EnumActionResult.SUCCESS, result.getType());
    }

    @Test
    void onPlayerStoppedUsing_normal()
    {
        final EntityPlayer player = testManager.getPlayer();
        final World world = spy(testManager.getWorld());

        ItemStack stack = new ItemStack(itemGrenade, 2, 0);
        //Trigger action
        itemGrenade.onPlayerStoppedUsing(stack, world, player, ItemGrenade.MAX_USE_DURATION);

        //Check that we spawned the entity
        final ArgumentCaptor<Entity> entityArgumentCaptor = ArgumentCaptor.forClass(Entity.class);
        verify(world, times(1)).spawnEntity(entityArgumentCaptor.capture());

        //Validate we spawned entity with the right settings
        Entity entity = entityArgumentCaptor.getValue();
        Assertions.assertTrue(entity instanceof EntityGrenade);
        Assertions.assertSame(player, ((EntityGrenade)entity).getThrower());
        Assertions.assertEquals(stack.getItem(), ((EntityGrenade)entity).explosive.toStack().getItem());

        //Check that we played a sound
        verify(world, times(1)).playSound(Mockito.isNull(), eq(player.posX), eq(player.posY), eq(player.posZ), eq(SoundEvents.ENTITY_TNT_PRIMED), eq(SoundCategory.BLOCKS), Mockito.anyFloat(), Mockito.anyFloat());

        //Check that we called shrink
        Assertions.assertEquals(1, stack.getCount());
    }
}
