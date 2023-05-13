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
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;

/**
 * Extended version of {@link ItemRemoteDetonator} that can target blocks in a line of sight.
 *
 *
 * Created by Dark(DarkGuardsman, Robert) on 3/26/2016.
 */
public class ItemLaserDetonator extends ItemRadio implements IPacketIDReceiver
{
    private static final int COOLDOWN = 20;
    private int clientCooldownTicks = 0;

    public ItemLaserDetonator()
    {
        this.setName("laserDetonator");
        this.setCreativeTab(ICBMClassic.CREATIVE_TAB);
        this.setHasSubtypes(true);
        this.setMaxStackSize(1);
        this.setNoRepair();
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand handIn)
    {
        final ItemStack stack = player.getHeldItem(handIn);
        if (world.isRemote && clientCooldownTicks <= 0)
        {
            clientCooldownTicks = COOLDOWN;
            RayTraceResult objectMouseOver = player.rayTrace(200, 1);
            if (objectMouseOver.typeOfHit != RayTraceResult.Type.MISS) // ignore failed raytraces
            {
                TileEntity tileEntity = world.getTileEntity(objectMouseOver.getBlockPos());
                if (!(ICBMClassicHelpers.isLauncher(tileEntity, null)))
                {
                    ICBMClassic.packetHandler.sendToServer(new PacketPlayerItem(player).addData(objectMouseOver.hitVec));
                }
            }// TODO else: add message stating that the raytrace failed
        }
        return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, stack);
    }

    @Override
    public ItemStack onItemUseFinish(ItemStack stack, World world, EntityLivingBase entityLiving) {

        if (world.isRemote) // when releasing the right mouse button, reset the cooldown to allow immediate reuse of item
            clientCooldownTicks = 0;
        return super.onItemUseFinish(stack, world, entityLiving);
    }

    @Override
    public void onUpdate(ItemStack stack, World world, Entity entity, int itemSlot, boolean isSelected) {
        if (world.isRemote && clientCooldownTicks > 0) // when holding the right mouse button, trigger item use every second
            clientCooldownTicks--;

        super.onUpdate(stack, world, entity, itemSlot, isSelected);
    }

    @Override
    public boolean read(ByteBuf buf, int id, EntityPlayer player, IPacket packet)
    {
        final ItemStack stack = player.inventory.getCurrentItem();
        if (stack.getItem() == this && !player.world.isRemote)
        {
            final double x = buf.readDouble();
            final double y = buf.readDouble();
            final double z = buf.readDouble();
            final Vec3d target = new Vec3d(x, y, z);

            // Fire on main thread
            ((WorldServer) player.getEntityWorld()).addScheduledTask(() -> {

                final LaserRemoteTriggerEvent event = new LaserRemoteTriggerEvent(player.world, target, player);
                if (!MinecraftForge.EVENT_BUS.post(event)) {
                    player.sendStatusMessage(new TextComponentTranslation(
                        getUnlocalizedName(stack) + ".target",
                        formatNumber(event.getPos().x),
                        formatNumber(event.getPos().y),
                        formatNumber(event.getPos().z)
                    ), false);

                    RadioRegistry.popMessage(player.world, new FakeRadioSender(player, stack, null), new TriggerActionTargetMessage(getRadioChannel(stack), event.getPos()));
                }
                else if(event.cancelReason != null) {
                    player.sendStatusMessage(new TextComponentTranslation(event.cancelReason), true);
                }
                else {
                    player.sendStatusMessage(new TextComponentTranslation(getUnlocalizedName(stack) + ".laser.canceled"), false);
                }
            });
        }
        return true;
    }

    private String formatNumber(double d) {
        return String.format("%.2f", d);
    }

    @Override
    public boolean doesSneakBypassUse(ItemStack stack, net.minecraft.world.IBlockAccess world, BlockPos pos, EntityPlayer player)
    {
        return true;
    }
}
