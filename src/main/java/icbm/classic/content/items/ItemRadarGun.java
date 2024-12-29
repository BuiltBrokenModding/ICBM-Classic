package icbm.classic.content.items;

import icbm.classic.ICBMClassic;
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
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

/**
 * Created by Dark(DarkGuardsman, Robin) on 6/13/2016.
 */
public class ItemRadarGun extends ItemBase implements IPacketIDReceiver
{
    public static final double MAX_RANGE = 200; //TODO config

    public ItemRadarGun(Properties p_i48487_1_) {
        super(p_i48487_1_);
    }


    @Override
    @Nullable
    public net.minecraftforge.common.capabilities.ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundNBT nbt)
    {
        final ItemStackCapProvider provider = new ItemStackCapProvider(stack);

        final IGPSData data = new CapabilityGPSDataItem(stack);
        provider.add("gps_data", ICBMClassicAPI.GPS_CAPABILITY, data);

        // Legacy logic from before IGPSData, v5.3.x
        if(nbt != null && nbt.contains("linkPos")) {
            final CompoundNBT save = nbt.getCompound("linkPos");
            data.setWorld(save.getInt("dimension"));
            data.setPosition(new Vec3d(save.getDouble("x"), save.getDouble("y"), save.getDouble("z")));
            nbt.remove("linkPos");
        }
        return provider;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> lines, ITooltipFlag flagIn)
    {
        // Stored data
        final IGPSData gpsData = ICBMClassicHelpers.getGPSData(stack);
        if(gpsData != null && gpsData.getPosition() != null) {
            final Vec3d pos = gpsData.getPosition();
            final World world = gpsData.getWorld();

            final String x = String.format("%.1f", pos.x);
            final String y = String.format("%.1f", pos.y);
            final String z = String.format("%.1f", pos.z);

            if(world != null) {
                final String name = Optional.of(world.getWorldInfo()).map(WorldInfo::getWorldName).orElse("--");
                final String worldName = String.format("(%s)%s", world.provider.getDimension(), name);
                final ITextComponent output = new TranslationTextComponent(getUnlocalizedName() + ".data.all", x, y, z, worldName);
                LanguageUtility.outputLines(output, lines::add);
            }
            else {
                final ITextComponent output = new TranslationTextComponent(getUnlocalizedName() + ".data.pos", x, y, z);
                LanguageUtility.outputLines(output, lines::add);
            }
        }
        else {
            LanguageUtility.outputLines(new TranslationTextComponent(getUnlocalizedName() + ".data.empty"), lines::add);
        }

        // General information
        final ITextComponent output = new TranslationTextComponent(getUnlocalizedName() + ".info", MAX_RANGE);
        LanguageUtility.outputLines(output, lines::add);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, Hand handIn)
    {
        if (player.isSneaking()) // also clear the gps coord if the play is shift-rightclicking in the air
        {
            if (!world.isRemote) {
                ItemStack stack = player.getHeldItem(handIn);
                stack.setTag(null);
                LanguageUtility.addChatToPlayer(player, "gps.cleared.name");
                player.container.detectAndSendChanges();
            }
            return new ActionResult<ItemStack>(ActionResultType.SUCCESS, player.getHeldItem(handIn));
        }

        if (world.isRemote)
        {
            RayTraceResult objectMouseOver = player.rayTrace(200, 1);
            if (objectMouseOver.typeOfHit != RayTraceResult.Type.MISS) { // TODO add message saying that the gps target is out of range.
                final TileEntity tileEntity = world.getTileEntity(objectMouseOver.getBlockPos());
                if (!(ICBMClassicHelpers.isLauncher(tileEntity, null))) {
                    sendToServer(player, handIn, objectMouseOver.hitVec);
                }
            }
        }
        return new ActionResult<ItemStack>(ActionResultType.SUCCESS, player.getHeldItem(handIn));
    }

    @Override
    public ActionResultType onItemUse(PlayerEntity player, World world, BlockPos pos, Hand hand, Direction facing, float hitX, float hitY, float hitZ)
    {
        final ItemStack stack = player.getHeldItem(hand);
        if (world.isRemote)
        {
            return ActionResultType.SUCCESS;
        }

        if (player.isSneaking())
        {
            stack.setTag(null);
            LanguageUtility.addChatToPlayer(player, "gps.cleared.name");
            player.container.detectAndSendChanges();
            return ActionResultType.SUCCESS;
        }
        else if(onTrace(new Vec3d(pos.getX() + hitX, pos.getY() + hitX, pos.getZ() + hitZ), player, stack)) {
            return ActionResultType.SUCCESS;
        }
        return ActionResultType.PASS;
    }

    public void sendToServer(PlayerEntity player, Hand hand, Vec3d hit) {
        ICBMClassic.packetHandler.sendToServer(new PacketPlayerItem(player).addData(hand == Hand.MAIN_HAND).addData(hit));
    }

    @Override
    public boolean read(ByteBuf buf, int id, PlayerEntity player, IPacket packet)
    {
        final Hand hand = buf.readBoolean() ? Hand.MAIN_HAND : Hand.OFF_HAND;
        final Vec3d pos = new Vec3d(buf.readDouble(), buf.readDouble(), buf.readDouble());
        if(player.world instanceof ServerWorld) {
            ((ServerWorld) player.world).getServer().execute(() -> {
                onTrace(pos, player, player.getHeldItem(hand));
            });
        }
        return true;
    }

    public boolean onTrace(final Vec3d posIn, PlayerEntity player, ItemStack stack)
    {
        if (stack.getItem() == this)
        {
            final RadarGunTraceEvent event = new RadarGunTraceEvent(player.world, posIn, player);

            if(MinecraftForge.EVENT_BUS.post(event) || event.pos == null) {
                //event was canceled
                return false; // TODO give user feedback
            }

            final IGPSData gpsData = ICBMClassicHelpers.getGPSData(stack);
            if(gpsData != null) {
                gpsData.setPosition(posIn);
                gpsData.setWorld(player.world);
                LanguageUtility.addChatToPlayer(player, "gps.pos.set.name");
            }
            // TODO give user feedback that something broke
        }
        return true;
    }
}
