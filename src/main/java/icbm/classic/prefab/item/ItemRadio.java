package icbm.classic.prefab.item;

import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.radio.IRadio;
import icbm.classic.api.radio.IRadioChannelAccess;
import icbm.classic.lib.radio.RadioRegistry;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.text.TranslationTextComponent;

@Deprecated /** @deprecated replace with capability */
public class ItemRadio extends ItemBase {

    private static final String MESSAGE_FREQUENCY_SET = "item.icbm.radio.frequency.set";

    public ItemRadio(Properties p_i48487_1_) {
        super(p_i48487_1_);
    }

    @Override
    public ActionResultType onItemUseFirst(ItemStack stack, ItemUseContext context)
    {
        final ItemStack heldItem = context.getPlayer().getHeldItem(context.getHand());
        final TileEntity tile = context.getWorld().getTileEntity(context.getPos());
        if(tile != null && tile.getCapability(ICBMClassicAPI.RADIO_CAPABILITY, context.getFace()).isPresent()) {
            if(!context.getWorld().isRemote) {
                final IRadio radio = tile.getCapability(ICBMClassicAPI.RADIO_CAPABILITY, context.getFace()).orElseThrow(IllegalStateException::new);
                if(radio instanceof IRadioChannelAccess) {
                    final String channel = ((IRadioChannelAccess) radio).getChannel();
                    setRadioChannel(heldItem, channel);
                    context.getPlayer().sendMessage(new TranslationTextComponent(MESSAGE_FREQUENCY_SET, channel));
                }
            }
            return ActionResultType.SUCCESS;
        }
        return ActionResultType.PASS;
    }

    /**
     * Gets the frequency this item broadcasts information on
     *
     * @param stack - this item
     * @return frequency
     */
    public String getRadioChannel(ItemStack stack)
    {
        if (stack.getTag() != null)
        {
            if(stack.getTag().contains("radio_channel")) {
                return stack.getTag().getString("radio_channel");
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
        stack.getOrCreateTag().putString("radio_channel", channel);
    }
}
