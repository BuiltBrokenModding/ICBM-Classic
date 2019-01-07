package icbm.classic.content.items;

import icbm.classic.lib.network.IPacket;
import icbm.classic.lib.network.IPacketIDReceiver;
import icbm.classic.lib.network.packet.PacketPlayerItem;
import icbm.classic.ICBMClassic;
import icbm.classic.lib.transform.vector.Pos;
import icbm.classic.lib.radio.RadioRegistry;
import icbm.classic.prefab.FakeRadioSender;
import icbm.classic.prefab.item.ItemICBMElectrical;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import icbm.classic.api.explosion.ILauncherController;

import java.util.List;

/**
 * Extended version of {@link ItemRemoteDetonator} that can target blocks in a line of sight.
 *
 *
 * Created by Dark(DarkGuardsman, Robert) on 3/26/2016.
 */
public class ItemLaserDetonator extends ItemICBMElectrical implements IPacketIDReceiver
{
    public ItemLaserDetonator()
    {
        super("laserDetonator");
        this.setHasSubtypes(true);
        this.setMaxStackSize(1);
        this.setNoRepair();
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand handIn)
    {
        ItemStack stack = player.getHeldItem(handIn);
        if (world.isRemote)
        {
            RayTraceResult objectMouseOver = player.rayTrace(200, 1);
            TileEntity tileEntity = world.getTileEntity(objectMouseOver.getBlockPos());
            if (!(tileEntity instanceof ILauncherController))
            {
                ICBMClassic.packetHandler.sendToServer(new PacketPlayerItem(player).addData(objectMouseOver.getBlockPos()));
            }
        }
        return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, stack);
    }

    @Override
    public boolean read(ByteBuf buf, int id, EntityPlayer player, IPacket packet)
    {
        ItemStack stack = player.inventory.getCurrentItem();
        if (stack != null && stack.getItem() == this)
        {
            if (!player.world.isRemote)
            {
                RadioRegistry.popMessage(player.world, new FakeRadioSender(player, stack, 2000), getBroadCastHz(stack), "activateLauncherWithTarget", new Pos(buf.readInt(), buf.readInt(), buf.readInt()));
            }
            else
            {
                player.sendMessage(new TextComponentString("Not encoded with launch data! Right click on launcher screen to encode."));
            }
        }
        return true;
    }

    @Override
    public boolean doesSneakBypassUse(ItemStack stack, net.minecraft.world.IBlockAccess world, BlockPos pos, EntityPlayer player)
    {
        return true;
    }

    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean b)
    {
        list.add("Fires missiles remotely");
        list.add("Right click launcher screen to encode");
    }

    /**
     * Gets the frequency this item broadcasts information on
     *
     * @param stack - this item
     * @return frequency
     */
    public float getBroadCastHz(ItemStack stack)
    {
        if (stack.getTagCompound() != null && stack.getTagCompound().hasKey("hz"))
        {
            return stack.getTagCompound().getFloat("hz");
        }
        return 0;
    }

    /**
     * Sets the frequency of this item
     *
     * @param stack - this item
     * @param hz    - value to set
     */
    public void setBroadCastHz(ItemStack stack, float hz)
    {
        if (stack.getTagCompound() == null)
        {
            stack.setTagCompound(new NBTTagCompound());
        }
        stack.getTagCompound().setFloat("hz", hz);
    }
}
