package icbm.classic.content.items;

import icbm.classic.ICBMConstants;
import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.events.LaserRemoteTriggerEvent;
import icbm.classic.lib.network.IPacket;
import icbm.classic.lib.network.IPacketIDReceiver;
import icbm.classic.lib.network.lambda.item.PacketCodexPlayerItem;
import icbm.classic.lib.network.lambda.item.PacketLambdaPlayerItem;
import icbm.classic.lib.network.netty.PacketManager;
import icbm.classic.lib.network.packet.PacketLaserDetonator;
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
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.*;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.MinecraftForge;

/**
 * Extended version of {@link ItemRemoteDetonator} that can target blocks in a line of sight.
 *
 *
 * Created by Dark(DarkGuardsman, Robin) on 3/26/2016.
 */
public class ItemLaserDetonator extends ItemRadio
{
    public static final ResourceLocation ID = new ResourceLocation(ICBMConstants.DOMAIN, "tool_detonator_laser");

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
            this.rayTraceOnClient(world, player, stack, handIn);

        }
        return new ActionResult<ItemStack>(ActionResultType.SUCCESS, stack);
    }


    private void rayTraceOnClient(World world, PlayerEntity player, ItemStack stack, Hand handIn) {
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
                PacketManager.sendToServer(new PacketLaserDetonator(world.dimension.getType().getId(), objectMouseOver.getHitVec(), handIn, getRadioChannel(stack)));
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
    public boolean doesSneakBypassUse(ItemStack stack, net.minecraft.world.IWorldReader world, BlockPos pos, PlayerEntity player)
    {
        return true;
    }
}
