package icbm.classic.world.item;

import icbm.classic.ICBMClassic;
import icbm.classic.IcbmConstants;
import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.ICBMClassicHelpers;
import icbm.classic.api.caps.IGPSData;
import icbm.classic.api.events.RadarGunTraceEvent;
import icbm.classic.lib.LanguageUtility;
import icbm.classic.lib.capability.gps.CapabilityGPSDataItem;
import icbm.classic.lib.network.IPacket;
import icbm.classic.lib.network.IPacketIDReceiver;
import icbm.classic.lib.network.packet.PacketPlayerItem;
import icbm.classic.prefab.item.ItemBase;
import icbm.classic.prefab.item.ItemStackCapProvider;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.WorldServer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.storage.WorldInfo;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.common.MinecraftForge;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

/**
 * Created by Dark(DarkGuardsman, Robert) on 6/13/2016.
 */
public class RadarGunItem extends ItemBase implements IPacketIDReceiver {
    public static final double MAX_RANGE = 200; //TODO config

    public RadarGunItem(Properties properties) {
        super(properties);
        this.setMaxStackSize(1);
        this.setHasSubtypes(true);
        this.setCreativeTab(ICBMClassic.CREATIVE_TAB);
        this.setUnlocalizedName(IcbmConstants.PREFIX + "radarGun");
        this.setRegistryName(IcbmConstants.MOD_ID, "radarGun");
    }

    @Override
    @Nullable
    public net.neoforged.common.capabilities.ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        final ItemStackCapProvider provider = new ItemStackCapProvider(stack);

        final IGPSData data = new CapabilityGPSDataItem(stack);
        provider.add("gps_data", ICBMClassicAPI.GPS_CAPABILITY, data);

        // Legacy logic from before IGPSData, v5.3.x
        if (nbt != null && nbt.contains("linkPos")) {
            final CompoundTag save = nbt.getCompound("linkPos");
            data.setLevel(save.getInteger("dimension"));
            data.setPosition(new Vec3(save.getDouble("x"), save.getDouble("y"), save.getDouble("z")));
            nbt.remove("linkPos");
        }
        return provider;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void addInformation(ItemStack stack, @Nullable Level levelIn, List<String> lines, ITooltipFlag flagIn) {
        // Stored data
        final IGPSData gpsData = ICBMClassicHelpers.getGPSData(stack);
        if (gpsData != null && gpsData.getPosition() != null) {
            final Vec3 pos = gpsData.getPosition();
            final Level level = gpsData.getLevel();

            final String x = String.format("%.1f", pos.x);
            final String y = String.format("%.1f", pos.y);
            final String z = String.format("%.1f", pos.z);

            if (world != null) {
                final String name = Optional.of(world.getLevelInfo()).map(WorldInfo::getLevelName).orElse("--");
                final String worldName = String.format("(%s)%s", world.provider.getDimension(), name);
                final Component output = new TextComponentTranslation(getUnlocalizedName() + ".data.all", x, y, z, worldName);
                LanguageUtility.outputLines(output, lines::add);
            } else {
                final Component output = new TextComponentTranslation(getUnlocalizedName() + ".data.pos", x, y, z);
                LanguageUtility.outputLines(output, lines::add);
            }
        } else {
            LanguageUtility.outputLines(new TextComponentTranslation(getUnlocalizedName() + ".data.empty"), lines::add);
        }

        // General information
        final Component output = new TextComponentTranslation(getUnlocalizedName() + ".info", MAX_RANGE);
        LanguageUtility.outputLines(output, lines::add);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(Level level, Player player, InteractionHand handIn) {
        if (player.isSneaking()) // also clear the gps coord if the play is shift-rightclicking in the air
        {
            if (!world.isClientSide()) {
                ItemStack stack = player.getHeldItem(handIn);
                stack.setTagCompound(null);
                stack.setItemDamage(0);
                LanguageUtility.addChatToPlayer(player, "gps.cleared.name");
                player.inventoryContainer.detectAndSendChanges();
            }
            return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, player.getHeldItem(handIn));
        }

        if (world.isClientSide()) {
            RayTraceResult objectMouseOver = player.rayTrace(200, 1);
            if (objectMouseOver.typeOfHit != RayTraceResult.Type.MISS) { // TODO add message saying that the gps target is out of range.
                final BlockEntity blockEntityEntity = world.getBlockEntity(objectMouseOver.getBlockPos());
                if (!(ICBMClassicHelpers.isLauncher(tileEntity, null))) {
                    sendToServer(player, handIn, objectMouseOver.hitVec);
                }
            }
        }
        return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, player.getHeldItem(handIn));
    }

    @Override
    public EnumActionResult onItemUse(Player player, Level level, BlockPos pos, InteractionHand hand, Direction facing, float hitX, float hitY, float hitZ) {
        final ItemStack stack = player.getHeldItem(hand);
        if (world.isClientSide()) {
            return EnumActionResult.SUCCESS;
        }

        if (player.isSneaking()) {
            stack.setTagCompound(null);
            stack.setItemDamage(0);
            LanguageUtility.addChatToPlayer(player, "gps.cleared.name");
            player.inventoryContainer.detectAndSendChanges();
            return EnumActionResult.SUCCESS;
        } else if (onTrace(new Vec3(pos.getX() + hitX, pos.getY() + hitX, pos.getZ() + hitZ), player, stack)) {
            return EnumActionResult.SUCCESS;
        }
        return EnumActionResult.PASS;
    }

    public void sendToServer(Player player, InteractionHand hand, Vec3 hit) {
        ICBMClassic.packetHandler.sendToServer(new PacketPlayerItem(player).addData(hand == InteractionHand.MAIN_HAND).addData(hit));
    }

    @Override
    public boolean read(ByteBuf buf, int id, Player player, IPacket packet) {
        final InteractionHand hand = buf.readBoolean() ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
        final Vec3 pos = new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble());
        if (player.world instanceof WorldServer) {
            ((WorldServer) player.world).addScheduledTask(() -> {
                onTrace(pos, player, player.getHeldItem(hand));
            });
        }
        return true;
    }

    public boolean onTrace(final Vec3 posIn, Player player, ItemStack stack) {
        if (stack.getItem() == this) {
            final RadarGunTraceEvent event = new RadarGunTraceEvent(player.world, posIn, player);

            if (MinecraftForge.EVENT_BUS.post(event) || event.pos == null) {
                //event was canceled
                return false; // TODO give user feedback
            }

            final IGPSData gpsData = ICBMClassicHelpers.getGPSData(stack);
            if (gpsData != null) {
                gpsData.setPosition(posIn);
                gpsData.setLevel(player.world);
                LanguageUtility.addChatToPlayer(player, "gps.pos.set.name");
            }
            // TODO give user feedback that something broke
        }
        return true;
    }
}
