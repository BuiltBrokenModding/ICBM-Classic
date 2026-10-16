package icbm.classic.content.items;

import icbm.classic.ICBMClassic;
import icbm.classic.api.ICBMClassicHelpers;
import icbm.classic.api.events.LaserRemoteTriggerEvent;
import icbm.classic.lib.network.IPacket;
import icbm.classic.lib.network.IPacketIDReceiver;
import icbm.classic.lib.network.packet.PacketPlayerItem;
import icbm.classic.lib.radio.RadioRegistry;
import icbm.classic.lib.radio.messages.TriggerActionTargetMessage;
import icbm.classic.prefab.FakeRadioSender;
import icbm.classic.prefab.item.ItemRadio;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;

/**
 * Extended version of {@link ItemRemoteDetonator} that can target blocks in a line of sight.
 *
 *
 * Created by Dark(DarkGuardsman, Robin) on 3/26/2016.
 */
public class ItemLaserDetonator extends ItemRadio implements IPacketIDReceiver
{
    private static final int COOLDOWN = 20;
    private int clientCooldownTicks = 0;

    public static final int RANGE = 200;

    public ItemLaserDetonator()
    {
        super(new Item.Properties().stacksTo(1).setNoRepair());
        this.setName("laserDetonator");
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand handIn)
    {
        final ItemStack stack = player.getItemInHand(handIn);
        if (world.isClientSide() && clientCooldownTicks <= 0)
        {
            clientCooldownTicks = COOLDOWN;
            final HitResult objectMouseOver = player.pick(RANGE, 1, false);
            if (objectMouseOver.getType() != HitResult.Type.MISS && objectMouseOver instanceof BlockHitResult blockHit) // ignore failed raytraces
            {
                final BlockEntity tileEntity = world.getBlockEntity(blockHit.getBlockPos());
                if (!(ICBMClassicHelpers.isLauncher(tileEntity, null)))
                {
                    // PORT: 1.12 always sent the selected hotbar slot, so off hand use reached the wrong item
                    final int slot = handIn == InteractionHand.MAIN_HAND ? player.getInventory().selected : Inventory.SLOT_OFFHAND;
                    ICBMClassic.packetHandler.sendToServer(new PacketPlayerItem(slot).addData(objectMouseOver.getLocation()));
                }
            }// TODO else: add message stating that the raytrace failed
            else {
                player.displayClientMessage(Component.translatable(getDescriptionId(stack) + ".laser.missed", RANGE), true);
            }
        }
        return InteractionResultHolder.success(stack);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level world, LivingEntity entityLiving) {

        if (world.isClientSide()) // when releasing the right mouse button, reset the cooldown to allow immediate reuse of item
            clientCooldownTicks = 0;
        return super.finishUsingItem(stack, world, entityLiving);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level world, Entity entity, int itemSlot, boolean isSelected) {
        if (world.isClientSide() && clientCooldownTicks > 0) // when holding the right mouse button, trigger item use every second
            clientCooldownTicks--;

        super.inventoryTick(stack, world, entity, itemSlot, isSelected);
    }

    @Override
    public boolean read(ByteBuf buf, int id, Player player, IPacket packet)
    {
        // Use the slot the packet was sent for, off hand included
        final ItemStack stack = packet instanceof PacketPlayerItem itemPacket && itemPacket.slotId >= 0
            ? player.getInventory().getItem(itemPacket.slotId)
            : player.getInventory().getSelected();
        if (stack.getItem() == this && !player.level().isClientSide())
        {
            final double x = buf.readDouble();
            final double y = buf.readDouble();
            final double z = buf.readDouble();
            final Vec3 target = new Vec3(x, y, z);

            // PORT: 1.12 trusted the client position, reject anything the client trace could not have hit
            if (!Double.isFinite(x) || !Double.isFinite(y) || !Double.isFinite(z)
                || player.getEyePosition().distanceToSqr(target) > (RANGE + 8) * (RANGE + 8))
            {
                return true;
            }

            // Packet handlers already run on the main thread
            final LaserRemoteTriggerEvent event = new LaserRemoteTriggerEvent(player.level(), target, player);
            if (!NeoForge.EVENT_BUS.post(event).isCanceled()) {
                player.displayClientMessage(Component.translatable(
                    getDescriptionId(stack) + ".target",
                    formatNumber(event.getPos().x),
                    formatNumber(event.getPos().y),
                    formatNumber(event.getPos().z)
                ), false);

                RadioRegistry.popMessage(player.level(), new FakeRadioSender(player, stack, null), new TriggerActionTargetMessage(getRadioChannel(stack), event.getPos()));
            }
            else if(event.cancelReason != null) {
                player.displayClientMessage(Component.translatable(event.cancelReason), true);
            }
            else {
                player.displayClientMessage(Component.translatable(getDescriptionId(stack) + ".laser.canceled"), false);
            }
        }
        return true;
    }

    private String formatNumber(double d) {
        return String.format("%.2f", d);
    }

    @Override
    public boolean doesSneakBypassUse(ItemStack stack, LevelReader world, BlockPos pos, Player player)
    {
        return true;
    }
}
// PORTED
