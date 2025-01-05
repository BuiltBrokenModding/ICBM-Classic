package icbm.classic.content.items;

import icbm.classic.ICBMClassic;
import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.events.LaserRemoteTriggerEvent;
import icbm.classic.lib.network.IPacket;
import icbm.classic.lib.network.IPacketIDReceiver;
import icbm.classic.lib.network.packet.PacketPlayerItem;
import icbm.classic.lib.radio.RadioRegistry;
import icbm.classic.lib.radio.messages.TriggerActionTargetMessage;
import icbm.classic.prefab.FakeRadioSender;
import icbm.classic.prefab.item.ItemRadio;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.*;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;

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

    public ItemLaserDetonator(Properties p_i48487_1_) {
        super(p_i48487_1_);
    }


    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, Hand handIn)
    {
        final ItemStack stack = player.getHeldItem(handIn);
        if (world.isRemote && clientCooldownTicks <= 0)
        {
            clientCooldownTicks = COOLDOWN;
            this.rayTraceOnClient(world, player, stack);

        }
        return new ActionResult<ItemStack>(ActionResultType.SUCCESS, stack);
    }


    private void rayTraceOnClient(World world, PlayerEntity player, ItemStack stack) {
        final BlockRayTraceResult objectMouseOver = world.rayTraceBlocks(new RayTraceContext(
            player.getEyePosition(1.0F),
            player.getLookVec().scale(RANGE).add(player.getEyePosition(1.0F)),
            RayTraceContext.BlockMode.OUTLINE,
            RayTraceContext.FluidMode.NONE, //TODO add a toggle to cycle between hitting fluids and not
            player
        ));

        if (objectMouseOver.getType() == RayTraceResult.Type.BLOCK)
        {
            final TileEntity tileEntity = world.getTileEntity(objectMouseOver.getPos());
            if (tileEntity == null || !tileEntity.getCapability(ICBMClassicAPI.MISSILE_LAUNCHER_CAPABILITY, objectMouseOver.getFace()).isPresent())
            {
                // TODO ICBMClassic.packetHandler.sendToServer(new PacketPlayerItem(player).addData(objectMouseOver.getHitVec()));
            }
        }
        else {
            player.sendStatusMessage(new TranslationTextComponent(getTranslationKey(stack) + ".laser.missed", RANGE), true);
        }
    }

    @Override
    public ItemStack onItemUseFinish(ItemStack stack, World world, LivingEntity entityLiving) {

        if (world.isRemote) // when releasing the right mouse button, reset the cooldown to allow immediate reuse of item
            clientCooldownTicks = 0;
        return super.onItemUseFinish(stack, world, entityLiving);
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int itemSlot, boolean isSelected) {
        if (world.isRemote && clientCooldownTicks > 0) // when holding the right mouse button, trigger item use every second
            clientCooldownTicks--;
        super.inventoryTick(stack, world, entity, itemSlot, isSelected);
    }

    @Override
    public boolean read(ByteBuf buf, int id, PlayerEntity player, IPacket packet)
    {
        final ItemStack stack = player.inventory.getCurrentItem();
        if (stack.getItem() == this && !player.world.isRemote)
        {
            final double x = buf.readDouble();
            final double y = buf.readDouble();
            final double z = buf.readDouble();
            final Vec3d target = new Vec3d(x, y, z);

            // Fire on main thread
            ((ServerWorld) player.getEntityWorld()).getServer().execute(() -> {

                final LaserRemoteTriggerEvent event = new LaserRemoteTriggerEvent(player.world, target, player);
                if (!MinecraftForge.EVENT_BUS.post(event)) {
                    player.sendStatusMessage(new TranslationTextComponent(
                        getTranslationKey(stack) + ".target",
                        formatNumber(event.getPos().x),
                        formatNumber(event.getPos().y),
                        formatNumber(event.getPos().z)
                    ), false);

                    RadioRegistry.popMessage(player.world, new FakeRadioSender(player, stack, null), new TriggerActionTargetMessage(getRadioChannel(stack), event.getPos()));
                }
                else if(event.cancelReason != null) {
                    player.sendStatusMessage(new TranslationTextComponent(event.cancelReason), true);
                }
                else {
                    player.sendStatusMessage(new TranslationTextComponent(getTranslationKey(stack) + ".laser.canceled"), false);
                }
            });
        }
        return true;
    }

    private String formatNumber(double d) {
        return String.format("%.2f", d);
    }

    @Override
    public boolean doesSneakBypassUse(ItemStack stack, net.minecraft.world.IWorldReader world, BlockPos pos, PlayerEntity player)
    {
        return true;
    }
}
