package icbm.classic.content.items;

import icbm.classic.ReflectionHelpers;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Bootstrap;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.lang.reflect.Field;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by Dark(DarkGuardsman, Robert) on 12/25/2019.
 */
public class TestItemGrenade
{
    final World world = Mockito.mock(World.class, RETURNS_DEEP_STUBS);
    final EntityPlayer player = Mockito.mock(EntityPlayer.class);
    final ItemGrenade itemGrenade = new ItemGrenade();

    @BeforeAll
    public static void beforeAll() {
        Bootstrap.register();
    }

    @BeforeEach
    public void beforeEach() throws Exception
    {
        //Reflective set field TODO honestly might just have to use fake worlds at this rate
        Field field = World.class.getDeclaredField("provider");
        ReflectionHelpers.removeFinal(field);
        field.set(world, mock(WorldProvider.class));
    }

    @Test
    void onItemRightClick_normal()
    {
        //Mock player held item
        when(player.getHeldItem(EnumHand.MAIN_HAND)).thenReturn(new ItemStack(itemGrenade));
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
        ItemStack stack = new ItemStack(itemGrenade, 2, 0);
        //Trigger action
        itemGrenade.onPlayerStoppedUsing(stack, world, player, ItemGrenade.MAX_USE_DURATION);

        //Check that we spawned the entity
        final ArgumentCaptor<Entity> entityArgumentCaptor = ArgumentCaptor.forClass(Entity.class);
        verify(world, times(1)).spawnEntity(entityArgumentCaptor.capture());

        //Check that we played a sound
        verify(world, times(1)).playSound(null, 0, 0, 0, SoundEvents.ENTITY_TNT_PRIMED, SoundCategory.BLOCKS, Mockito.anyFloat(), Mockito.anyFloat());

        //Check that we called shrink
        Assertions.assertEquals(1, stack.getCount());
    }
}
