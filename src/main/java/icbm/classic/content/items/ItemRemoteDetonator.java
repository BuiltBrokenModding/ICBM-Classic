package icbm.classic.content.items;

import icbm.classic.api.events.RemoteTriggerEvent;
import icbm.classic.lib.radio.RadioRegistry;
import icbm.classic.lib.radio.messages.TriggerActionMessage;
import icbm.classic.prefab.FakeRadioSender;
import icbm.classic.prefab.item.ItemRadio;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.neoforged.neoforge.common.NeoForge;

/**
 * Remotely triggers missile launches on a set frequency, call back ID, and pass key. Will not funciton if any of those
 * data points is missing.
 *
 *
 * Created by Dark(DarkGuardsman, Robin) on 3/26/2016.
 */
public class ItemRemoteDetonator extends ItemRadio
{
    public ItemRemoteDetonator()
    {
        super(new Item.Properties().stacksTo(1).setNoRepair());
        this.setName("remoteDetonator");
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand handIn)
    {
        ItemStack stack = player.getItemInHand(handIn);
        if (!world.isClientSide())
        {
            if(!NeoForge.EVENT_BUS.post(new RemoteTriggerEvent(world, player, stack)).isCanceled()) //event was not canceled
            {
                final String channel = getRadioChannel(stack);
                RadioRegistry.popMessage(world, new FakeRadioSender(player, stack, null), new TriggerActionMessage(channel));
            }
        }
        return InteractionResultHolder.success(stack);
    }

    @Override
    public boolean doesSneakBypassUse(ItemStack stack, LevelReader world, BlockPos pos, Player player)
    {
        return true;
    }
}
// PORTED
