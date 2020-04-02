package icbm.classic.content.items;

import icbm.classic.ICBMClassic;
import icbm.classic.ICBMConstants;
import icbm.classic.api.ICBMClassicHelpers;
import icbm.classic.api.data.IWorldPosition;
import icbm.classic.api.caps.IMissileLauncher;
import icbm.classic.api.events.RadarGunTraceEvent;
import icbm.classic.api.items.IWorldPosItem;
import icbm.classic.api.tile.multiblock.IMultiTile;
import icbm.classic.api.tile.multiblock.IMultiTileHost;
import icbm.classic.lib.LanguageUtility;
import icbm.classic.lib.network.IPacket;
import icbm.classic.lib.network.IPacketIDReceiver;
import icbm.classic.lib.network.packet.PacketPlayerItem;
import icbm.classic.lib.transform.vector.Location;
import icbm.classic.prefab.item.ItemBase;
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
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Created by Dark(DarkGuardsman, Robert) on 6/13/2016.
 */
public class ItemRadarGun extends ItemBase implements IWorldPosItem, IPacketIDReceiver
{
    public static final String NBT_LINK_POS = "linkPos";

    public ItemRadarGun()
    {
        this.setMaxStackSize(1);
        this.setHasSubtypes(true);
        this.setCreativeTab(ICBMClassic.CREATIVE_TAB);
        this.setTranslationKey(ICBMConstants.PREFIX + "radarGun");
        this.setRegistryName(ICBMConstants.DOMAIN, "radarGun");
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> lines, ITooltipFlag flagIn)
    {
        String localization = LanguageUtility.getLocal(getTranslationKey() + ".info");
        if (localization != null && !localization.isEmpty())
        {
            String[] split = localization.split(",");
            for (String line : split)
            {
                lines.add(line.trim());
            }
        }
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
                    ICBMClassic.packetHandler.sendToServer(new PacketPlayerItem(player).addData(objectMouseOver.getBlockPos()));
                }
            }
        }
        return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, player.getHeldItem(handIn));
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
    {
        ItemStack stack = player.getHeldItem(hand);
        if (world.isRemote)
        {
            return EnumActionResult.SUCCESS;
        }

        Location location = new Location(world, pos);
        TileEntity tile = location.getTileEntity();
        if (tile instanceof IMultiTile)
        {
            IMultiTileHost host = ((IMultiTile) tile).getHost();
            if (host instanceof TileEntity)
            {
                tile = (TileEntity) host;
            }
        }

        if (player.isSneaking())
        {
            stack.setTagCompound(null);
            stack.setItemDamage(0);
            LanguageUtility.addChatToPlayer(player, "gps.cleared.name");
            player.inventoryContainer.detectAndSendChanges();
            return EnumActionResult.SUCCESS;
        }
        else
        {
            Location storedLocation = getLocation(stack);


            if (ICBMClassicHelpers.isLauncher(tile, facing))
            {
                if(storedLocation == null)
                {
                    LanguageUtility.addChatToPlayer(player, "gps.error.pos.invalid.null.name");
                    return EnumActionResult.SUCCESS;
                }
                else if (!storedLocation.isAboveBedrock())
                {
                    LanguageUtility.addChatToPlayer(player, "gps.error.pos.invalid.name");
                    return EnumActionResult.SUCCESS;
                }
                else if(storedLocation.world != world)
                {
                    LanguageUtility.addChatToPlayer(player, "gps.error.pos.invalid.world.name");
                    return EnumActionResult.SUCCESS;
                }
                else
                {
                    final IMissileLauncher launcher = ICBMClassicHelpers.getLauncher(tile, facing);
                    if (launcher != null)
                    {
                        launcher.setTarget(storedLocation.x(), storedLocation.y(), storedLocation.z());
                        LanguageUtility.addChatToPlayer(player, "gps.data.transferred.name");
                        return EnumActionResult.SUCCESS;
                    }
                }
            }
            else if(trace(pos, player)) // otherwise, save the currently clicked block as a location if the trace event has not been canceled
                return EnumActionResult.SUCCESS;
        }
        return EnumActionResult.PASS;
    }

    @Override
    public boolean read(ByteBuf buf, int id, EntityPlayer player, IPacket packet)
    {
        return trace(buf.readInt(), buf.readInt(), buf.readInt(), player);
    }

    public boolean trace(int x, int y, int z, EntityPlayer player)
    {
        return trace(new BlockPos(x, y, z), player);
    }

    public boolean trace(BlockPos pos, EntityPlayer player)
    {
        ItemStack stack = player.inventory.getCurrentItem();
        if (stack != null && stack.getItem() == this)
        {
            RadarGunTraceEvent event = new RadarGunTraceEvent(player.world, pos, player);

            if(MinecraftForge.EVENT_BUS.post(event)) //event was canceled
                return false;

            if(event.pos == null) //someone set the pos in the event to null, use original data
                setLocation(stack, new Location(player.world, pos.getX(), pos.getY(), pos.getZ()));
            else
                setLocation(stack, new Location(player.world, event.pos.getX(), event.pos.getY(), event.pos.getZ()));

            LanguageUtility.addChatToPlayer(player, "gps.pos.set.name");
            System.out.println(getLocation(stack));
        }
        return true;
    }

    @Override
    public Location getLocation(ItemStack stack)
    {
        if (stack.getTagCompound() != null && stack.getTagCompound().hasKey(NBT_LINK_POS))
        {
            return new Location(stack.getTagCompound().getCompoundTag(NBT_LINK_POS));
        }
        return null;
    }

    @Override
    public void setLocation(ItemStack stack, IWorldPosition loc)
    {
        if (loc != null)
        {
            if (stack.getTagCompound() == null)
            {
                stack.setTagCompound(new NBTTagCompound());
            }

            NBTTagCompound save = new NBTTagCompound();
            if (loc instanceof Location)
            {
                ((Location) loc).writeNBT(save);
            }
            else
            {
                new Location(loc).writeNBT(save);
            }
            stack.getTagCompound().setTag(NBT_LINK_POS, save);
        }
        else if (stack.getTagCompound() != null)
        {
            stack.getTagCompound().removeTag(NBT_LINK_POS);
        }
    }

    @Override
    public boolean canAccessLocation(ItemStack stack, Object obj)
    {
        return false;
    }
}
