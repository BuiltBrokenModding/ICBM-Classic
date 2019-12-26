package icbm.classic.content.items;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by Dark(DarkGuardsman, Robert) on 12/25/2019.
 */
public class TestItemGrenade
{
    final World world = Mockito.mock(World.class);
    final EntityPlayer player = Mockito.mock(EntityPlayer.class);

    @Test
    void onItemRightClick_normal()
    {
        final ItemGrenade itemGrenade = new ItemGrenade();
        final ItemStack itemStack = ItemStack.EMPTY;

        //Mock player held item
        when(player.getHeldItem(EnumHand.MAIN_HAND)).thenReturn(itemStack);
        //Prevent normal set active logic as this will crash if run
        doNothing().when(player).setActiveHand(EnumHand.MAIN_HAND);

        //Trigger action
        final ActionResult<ItemStack> result = itemGrenade.onItemRightClick(world, player, EnumHand.MAIN_HAND);

        //Main hand should be set
        verify(player, times(1)).setActiveHand(EnumHand.MAIN_HAND);

        //Check result
        Assertions.assertEquals(itemStack, result.getResult());
        Assertions.assertEquals(EnumActionResult.SUCCESS, result.getType());
    }
}
