package icbm.classic.prefab.item;

import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.radio.IRadio;
import icbm.classic.api.radio.IRadioChannelAccess;
import icbm.classic.lib.NBTConstants;
import icbm.classic.lib.StackNbt;
import icbm.classic.lib.radio.RadioRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

public class ItemRadio extends ItemBase {

    public ItemRadio()
    {
        super();
    }

    public ItemRadio(Item.Properties properties)
    {
        super(properties);
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack heldItem, UseOnContext context)
    {
        final Level world = context.getLevel();
        final Player player = context.getPlayer();
        final IRadio radio = world.getCapability(ICBMClassicAPI.RADIO_CAPABILITY, context.getClickedPos(), context.getClickedFace());
        if(radio != null) {
            if(!world.isClientSide()) {
                if(radio instanceof IRadioChannelAccess) {
                    final String channel = ((IRadioChannelAccess) radio).getChannel();
                    setRadioChannel(heldItem, channel);
                    if(player != null) {
                        // Lang entry uses %s for the channel
                        player.sendSystemMessage(Component.translatable("chat.launcher.toolFrequencySet", "" + channel));
                    }
                }
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    /**
     * Gets the frequency this item broadcasts information on
     *
     * @param stack - this item
     * @return frequency
     */
    public String getRadioChannel(ItemStack stack) //TODO move to capability item
    {
        final CompoundTag tag = StackNbt.get(stack);
        if(tag.contains(NBTConstants.HZ)) {
            return Integer.toString((int)Math.floor(tag.getFloat(NBTConstants.HZ)));
        }
        else if(tag.contains("radio_channel")) {
            return tag.getString("radio_channel");
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
        StackNbt.update(stack, tag -> {
            tag.remove(NBTConstants.HZ);
            tag.putString("radio_channel", channel);
        });
    }
}
// PORTED
