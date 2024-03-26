package icbm.classic.world.item;

import icbm.classic.ICBMClassic;
import icbm.classic.api.events.RemoteTriggerEvent;
import icbm.classic.lib.radio.RadioRegistry;
import icbm.classic.lib.radio.messages.TriggerActionMessage;
import icbm.classic.prefab.FakeRadioSender;
import icbm.classic.prefab.item.ItemRadio;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.common.MinecraftForge;

/**
 * Remotely triggers missile launches on a set frequency, call back ID, and pass key. Will not funciton if any of those
 * data points is missing.
 * <p>
 * <p>
 * Created by Dark(DarkGuardsman, Robert) on 3/26/2016.
 */
public class RemoteDetonatorItem extends ItemRadio {
    public RemoteDetonatorItem(Properties properties) {
        super(properties);
        this.setName("remoteDetonator");
        this.setCreativeTab(ICBMClassic.CREATIVE_TAB);
        this.setHasSubtypes(true);
        this.setMaxStackSize(1);
        this.setNoRepair();
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(Level level, Player player, InteractionHand handIn) {
        ItemStack stack = player.getHeldItem(handIn);
        if (!world.isClientSide()) {
            if (!MinecraftForge.EVENT_BUS.post(new RemoteTriggerEvent(world, player, stack))) //event was not canceled
            {
                final String channel = getRadioChannel(stack);
                RadioRegistry.popMessage(world, new FakeRadioSender(player, stack, null), new TriggerActionMessage(channel));
            }
        }
        return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, stack);
    }

    @Override
    public boolean doesSneakBypassUse(ItemStack stack, net.minecraft.world.IBlockAccess world, BlockPos pos, Player player) {
        return true;
    }
}
