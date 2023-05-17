package icbm.classic.content.items;

import icbm.classic.ICBMClassic;
import icbm.classic.ICBMConstants;
import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.ICBMClassicHelpers;
import icbm.classic.api.caps.IGPSData;
import icbm.classic.api.events.RadarGunTraceEvent;
import icbm.classic.lib.LanguageUtility;
import icbm.classic.lib.capability.gps.CapabilityGPSData;
import icbm.classic.lib.capability.gps.CapabilityGPSDataItem;
import icbm.classic.lib.network.IPacket;
import icbm.classic.lib.network.IPacketIDReceiver;
import icbm.classic.lib.network.packet.PacketPlayerItem;
import icbm.classic.lib.transform.vector.Location;
import icbm.classic.prefab.gui.GuiContainerBase;
import icbm.classic.prefab.item.ItemBase;
import icbm.classic.prefab.item.ItemStackCapProvider;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

/**
 * Created by Dark(DarkGuardsman, Robert) on 6/13/2016.
 */
public class ItemRadarGun extends ItemBase implements IPacketIDReceiver
{
    public static final double MAX_RANGE = 200; //TODO config

    public ItemRadarGun()
    {
        this.setMaxStackSize(1);
        this.setHasSubtypes(true);
        this.setCreativeTab(ICBMClassic.CREATIVE_TAB);
        this.setUnlocalizedName(ICBMConstants.PREFIX + "radarGun");
        this.setRegistryName(ICBMConstants.DOMAIN, "radarGun");
    }

    @Override
    @Nullable
    public net.minecraftforge.common.capabilities.ICapabilityProvider initCapabilities(ItemStack stack, @Nullable NBTTagCompound nbt)
    {
        final ItemStackCapProvider provider = new ItemStackCapProvider(stack);

        final IGPSData data = new CapabilityGPSDataItem(stack);
        provider.add("gps_data", ICBMClassicAPI.GPS_CAPABILITY, data);

        // Legacy logic from before IGPSData, v5.3.x
        if(nbt != null && nbt.hasKey("linkPos")) {
            final NBTTagCompound save = nbt.getCompoundTag("linkPos");
            data.setWorld(save.getInteger("dimension"));
            data.setPosition(new Vec3d(save.getDouble("x"), save.getDouble("y"), save.getDouble("z")));
            nbt.removeTag("linkPos");
        }
        return provider;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> lines, ITooltipFlag flagIn)
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
                final ITextComponent output = new TextComponentTranslation(getUnlocalizedName() + ".data.all", x, y, z, worldName);
                LanguageUtility.outputLines(output, lines::add);
            }
            else {
                final ITextComponent output = new TextComponentTranslation(getUnlocalizedName() + ".data.pos", x, y, z);
                LanguageUtility.outputLines(output, lines::add);
            }
        }
        else {
            LanguageUtility.outputLines(new TextComponentTranslation(getUnlocalizedName() + ".data.empty"), lines::add);
        }

        // General information
        final ITextComponent output = new TextComponentTranslation(getUnlocalizedName() + ".info", MAX_RANGE);
        LanguageUtility.outputLines(output, lines::add);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand handIn)
    {
        if (player.isSneaking()) // also clear the gps coord if the play is shift-rightclicking in the air
        {
            if (!world.isRemote) {
                ItemStack stack = player.getHeldItem(handIn);
                stack.setTagCompound(null);
                stack.setItemDamage(0);
                LanguageUtility.addChatToPlayer(player, "gps.cleared.name");
                player.inventoryContainer.detectAndSendChanges();
            }
            return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, player.getHeldItem(handIn));
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
        return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, player.getHeldItem(handIn));
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
    {
        final ItemStack stack = player.getHeldItem(hand);
        if (world.isRemote)
        {
            return EnumActionResult.SUCCESS;
        }

        if (player.isSneaking())
        {
            stack.setTagCompound(null);
            stack.setItemDamage(0);
            LanguageUtility.addChatToPlayer(player, "gps.cleared.name");
            player.inventoryContainer.detectAndSendChanges();
            return EnumActionResult.SUCCESS;
        }
        else if(onTrace(new Vec3d(pos.getX() + hitX, pos.getY() + hitX, pos.getZ() + hitZ), player, stack)) {
            return EnumActionResult.SUCCESS;
        }
        return EnumActionResult.PASS;
    }

    public void sendToServer(EntityPlayer player, EnumHand hand, Vec3d hit) {
        ICBMClassic.packetHandler.sendToServer(new PacketPlayerItem(player).addData(hand == EnumHand.MAIN_HAND).addData(hit));
    }

    @Override
    public boolean read(ByteBuf buf, int id, EntityPlayer player, IPacket packet)
    {
        final EnumHand hand = buf.readBoolean() ? EnumHand.MAIN_HAND : EnumHand.OFF_HAND;
        final Vec3d pos = new Vec3d(buf.readDouble(), buf.readDouble(), buf.readDouble());
        if(player.world instanceof WorldServer) {
            ((WorldServer) player.world).addScheduledTask(() -> {
                onTrace(pos, player, player.getHeldItem(hand));
            });
        }
        return true;
    }

    public boolean onTrace(final Vec3d posIn, EntityPlayer player, ItemStack stack)
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
