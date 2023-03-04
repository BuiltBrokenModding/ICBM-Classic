package icbm.classic.prefab.item;

import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.radio.IRadio;
import icbm.classic.api.radio.IRadioChannelAccess;
import icbm.classic.lib.LanguageUtility;
import icbm.classic.lib.NBTConstants;
import icbm.classic.lib.radio.RadioRegistry;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

public class ItemRadio extends ItemBase {

    @Override
    public EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand)
    {
        final ItemStack heldItem = player.getHeldItem(hand);
        final TileEntity tile = world.getTileEntity(pos);
        if(tile != null && tile.hasCapability(ICBMClassicAPI.RADIO_CAPABILITY, side)) {
            if(!world.isRemote) {
                final IRadio radio = tile.getCapability(ICBMClassicAPI.RADIO_CAPABILITY, side);
                if(radio instanceof IRadioChannelAccess) {
                    final String channel = ((IRadioChannelAccess) radio).getChannel();
                    setRadioChannel(heldItem, channel);
                    player.sendMessage(new TextComponentString(LanguageUtility.getLocal("chat.launcher.toolFrequencySet").replace("%s", "" + channel)));
                }
            }
            return EnumActionResult.SUCCESS;
        }
        return EnumActionResult.PASS;
    }

    /**
     * Gets the frequency this item broadcasts information on
     *
     * @param stack - this item
     * @return frequency
     */
    public String getRadioChannel(ItemStack stack) //TODO move to capability item
    {
        if (stack.getTagCompound() != null)
        {
            if(stack.getTagCompound().hasKey(NBTConstants.HZ)) {
                return Integer.toString((int)Math.floor(stack.getTagCompound().getFloat(NBTConstants.HZ)));
            }
            else if(stack.getTagCompound().hasKey("radio_channel")) {
                return stack.getTagCompound().getString("radio_channel");
            }
        }
        return RadioRegistry.EMPTY_HZ;
    }

    /**
     * Sets the frequency of this item
     *
     * @param stack - this item
     * @param channel    - value to set
     */
    public void setRadioChannel(ItemStack stack, String channel)
    {
        if (stack.getTagCompound() == null)
        {
            stack.setTagCompound(new NBTTagCompound());
        }
        if(stack.getTagCompound().hasKey(NBTConstants.HZ)) {
            stack.getTagCompound().removeTag(NBTConstants.HZ);
        }
        stack.getTagCompound().setString("radio_channel", channel);
    }
}
