package icbm.classic.prefab.item;

import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.radio.IRadio;
import icbm.classic.api.radio.IRadioChannelAccess;
import icbm.classic.lib.LanguageUtility;
import icbm.classic.lib.NBTConstants;
import icbm.classic.lib.radio.RadioRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class ItemRadio extends ItemBase {

    public ItemRadio(Properties properties) {
        super(properties);
    }

    @Override
    public EnumActionResult onItemUseFirst(Player player, Level level, BlockPos pos, Direction side, float hitX, float hitY, float hitZ, InteractionHand hand) {
        final ItemStack heldItem = player.getHeldItem(hand);
        final BlockEntity blockEntity = world.getBlockEntity(pos);
        if (tile != null && tile.hasCapability(ICBMClassicAPI.RADIO_CAPABILITY, side)) {
            if (!world.isClientSide()) {
                final IRadio radio = tile.getCapability(ICBMClassicAPI.RADIO_CAPABILITY, side);
                if (radio instanceof IRadioChannelAccess) {
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
        if (stack.getTagCompound() != null) {
            if (stack.getTagCompound().contains(NBTConstants.HZ)) {
                return Integer.toString((int) Math.floor(stack.getTagCompound().getFloat(NBTConstants.HZ)));
            } else if (stack.getTagCompound().contains("radio_channel")) {
                return stack.getTagCompound().getString("radio_channel");
            }
        }
        return RadioRegistry.EMPTY_HZ;
    }

    /**
     * Sets the frequency of this item
     *
     * @param stack   - this item
     * @param channel - value to set
     */
    public void setRadioChannel(ItemStack stack, String channel) {
        if (stack.getTagCompound() == null) {
            stack.setTagCompound(new CompoundTag());
        }
        if (stack.getTagCompound().contains(NBTConstants.HZ)) {
            stack.getTagCompound().remove(NBTConstants.HZ);
        }
        stack.getTagCompound().putString("radio_channel", channel);
    }
}
