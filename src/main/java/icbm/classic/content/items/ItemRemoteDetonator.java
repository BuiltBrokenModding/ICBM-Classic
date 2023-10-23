package icbm.classic.content.items;

import icbm.classic.ICBMClassic;
import icbm.classic.api.events.RemoteTriggerEvent;
import icbm.classic.lib.radio.RadioRegistry;
import icbm.classic.lib.radio.messages.TriggerActionMessage;
import icbm.classic.prefab.FakeRadioSender;
import icbm.classic.prefab.item.ItemRadio;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;

/**
 * Remotely triggers missile launches on a set frequency, call back ID, and pass key. Will not funciton if any of those
 * data points is missing.
 *
 *
 * Created by Dark(DarkGuardsman, Robert) on 3/26/2016.
 */
public class ItemRemoteDetonator extends ItemRadio
{
    public ItemRemoteDetonator()
    {
        this.setName("remoteDetonator");
        this.setCreativeTab(ICBMClassic.CREATIVE_TAB);
        this.setHasSubtypes(true);
        this.setMaxStackSize(1);
        this.setNoRepair();
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand handIn)
    {
        ItemStack stack = player.getHeldItem(handIn);
        if (!world.isRemote)
        {
            if(!MinecraftForge.EVENT_BUS.post(new RemoteTriggerEvent(world, player, stack))) //event was not canceled
            {
                final String channel = getRadioChannel(stack);
                RadioRegistry.popMessage(world, new FakeRadioSender(player, stack, null), new TriggerActionMessage(channel));
            }
        }
        return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, stack);
    }

    @Override
    public boolean doesSneakBypassUse(ItemStack stack, net.minecraft.world.IBlockAccess world, BlockPos pos, EntityPlayer player)
    {
        return true;
    }
}
