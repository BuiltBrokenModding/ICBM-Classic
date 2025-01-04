package icbm.classic.content.items;

import icbm.classic.ICBMClassic;
import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.caps.IGPSData;
import icbm.classic.api.events.RadarGunTraceEvent;
import icbm.classic.lib.LanguageUtility;
import icbm.classic.lib.capability.gps.CapabilityGPSData;
import icbm.classic.lib.network.IPacket;
import icbm.classic.lib.network.IPacketIDReceiver;
import icbm.classic.lib.network.packet.PacketPlayerItem;
import icbm.classic.prefab.item.ItemBase;
import icbm.classic.prefab.item.ItemStackCapProvider;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

/**
 * Created by Dark(DarkGuardsman, Robin) on 6/13/2016.
 */
public class ItemRadarGun extends ItemBase implements IPacketIDReceiver {
    public static final double MAX_RANGE = 200; //TODO config

    public ItemRadarGun(Properties p_i48487_1_) {
        super(p_i48487_1_);
    }


    @Override
    @Nullable
    public net.minecraftforge.common.capabilities.ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundNBT nbt) {
        return new ItemStackCapProvider(stack).with(ICBMClassicAPI.GPS_CAPABILITY, CapabilityGPSData::new);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> lines, ITooltipFlag flagIn) {
        // Stored data
        final LazyOptional<IGPSData> gpsData = stack.getCapability(ICBMClassicAPI.GPS_CAPABILITY, null);
        if (gpsData.isPresent() && gpsData.orElseThrow(IllegalStateException::new).getPosition() != null) {
            final Vec3d pos = gpsData.orElseThrow(IllegalStateException::new).getPosition();
            final DimensionType dimensionType = Optional.ofNullable(gpsData.orElseThrow(IllegalStateException::new).getDimensionKey()).map(DimensionType::byName).orElse(null);

            final String x = String.format("%.1f", pos.x);
            final String y = String.format("%.1f", pos.y);
            final String z = String.format("%.1f", pos.z);

            if (dimensionType  != null) {
                final String name = dimensionType.toString(); //TODO find a way to translate
                final String worldName = String.format("(%s)%s", dimensionType.getId(), name);
                final ITextComponent output = new TranslationTextComponent(getTranslationKey(stack) + ".data.all", x, y, z, worldName);
                LanguageUtility.outputComponents(output, lines::add);
            } else {
                final ITextComponent output = new TranslationTextComponent(getTranslationKey(stack) + ".data.pos", x, y, z);
                LanguageUtility.outputComponents(output, lines::add);
            }
        } else {
            LanguageUtility.outputComponents(new TranslationTextComponent(getTranslationKey(stack) + ".data.empty"), lines::add);
        }

        // General information
        final ITextComponent output = new TranslationTextComponent(getTranslationKey(stack) + ".info", MAX_RANGE);
        LanguageUtility.outputComponents(output, lines::add);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, Hand handIn) {
        final ItemStack stack = player.getHeldItem(handIn);
        if (player.isSneaking()) {
            if (!world.isRemote) {
                LanguageUtility.addChatToPlayer(player, "gps.cleared.name");
                return clearStoredTarget(stack);
            }
            return new ActionResult<ItemStack>(ActionResultType.SUCCESS, player.getHeldItem(handIn));
        } else if (world.isRemote) {
            rayTraceOnClient(world, player, handIn, stack);
        }
        return new ActionResult<ItemStack>(ActionResultType.SUCCESS, player.getHeldItem(handIn));
    }

    private ActionResult<ItemStack> clearStoredTarget(ItemStack stack) {
        final ItemStack result = stack.copy();
        result.setTag(null);
        return new ActionResult<ItemStack>(ActionResultType.SUCCESS, result);
    }

    private void rayTraceOnClient(World world, PlayerEntity player, Hand handIn, ItemStack stack) {
        final BlockRayTraceResult objectMouseOver = world.rayTraceBlocks(new RayTraceContext(
            player.getEyePosition(1.0F),
            player.getLookVec().scale(MAX_RANGE).add(player.getEyePosition(1.0F)),
            RayTraceContext.BlockMode.OUTLINE,
            RayTraceContext.FluidMode.NONE, //TODO add a toggle to cycle between hitting fluids and not
            player
        ));
        if (objectMouseOver.getType() == RayTraceResult.Type.BLOCK) { // TODO add message saying that the gps target is out of range.
            final TileEntity tileEntity = world.getTileEntity(objectMouseOver.getPos());
            if (tileEntity == null || !tileEntity.getCapability(ICBMClassicAPI.MISSILE_LAUNCHER_CAPABILITY, objectMouseOver.getFace()).isPresent()) {
                sendToServer(player, handIn, objectMouseOver.getHitVec());
            }
        } else {
            player.sendStatusMessage(new TranslationTextComponent(getTranslationKey(stack) + ".laser.missed", MAX_RANGE), true);
        }
    }

    @Override
    public ActionResultType onItemUse(ItemUseContext context) {
        final ItemStack stack = context.getPlayer().getHeldItem(context.getHand());
        if (context.getWorld().isRemote) {
            return ActionResultType.SUCCESS;
        }

        if (context.isPlacerSneaking()) {
            stack.setTag(null);
            context.getPlayer().container.detectAndSendChanges();

            LanguageUtility.addChatToPlayer(context.getPlayer(), "gps.cleared.name");
            return ActionResultType.SUCCESS;
        }
        else if (onGpsData(new Vec3d(context.getHitVec().x, context.getHitVec().y, context.getHitVec().z), context.getPlayer(), stack)) {
            return ActionResultType.SUCCESS;
        }
        return ActionResultType.PASS;
    }

    public void sendToServer(PlayerEntity player, Hand hand, Vec3d hit) {
        ICBMClassic.packetHandler.sendToServer(new PacketPlayerItem(player).addData(hand == Hand.MAIN_HAND).addData(hit));
    }

    @Override
    public boolean read(ByteBuf buf, int id, PlayerEntity player, IPacket packet) {
        final Hand hand = buf.readBoolean() ? Hand.MAIN_HAND : Hand.OFF_HAND;
        final Vec3d pos = new Vec3d(buf.readDouble(), buf.readDouble(), buf.readDouble());
        if (player.world instanceof ServerWorld) {
            ((ServerWorld) player.world).getServer().execute(() -> {
                onGpsData(pos, player, player.getHeldItem(hand));
            });
        }
        return true;
    }

    public boolean onGpsData(final Vec3d posIn, PlayerEntity player, ItemStack stack) {
        if (stack.getItem() == this) {
            final RadarGunTraceEvent event = new RadarGunTraceEvent(player.world, posIn, player);

            if (MinecraftForge.EVENT_BUS.post(event) || event.pos == null) {
                //event was canceled
                return false; // TODO give user feedback
            }

            final LazyOptional<IGPSData> gpsData = stack.getCapability(ICBMClassicAPI.GPS_CAPABILITY);
            if (gpsData.isPresent()) {
                gpsData.orElseThrow(IllegalStateException::new).setPosition(posIn);
                gpsData.orElseThrow(IllegalStateException::new).setWorld(player.world);
                LanguageUtility.addChatToPlayer(player, "gps.pos.set.name");
            }
            // TODO give user feedback that something broke
        }
        return true;
    }
}
