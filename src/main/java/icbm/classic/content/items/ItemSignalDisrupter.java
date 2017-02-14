package icbm.classic.content.items;

import com.builtbroken.mc.api.items.hz.IItemFrequency;
import com.builtbroken.mc.api.tile.IGuiTile;
import com.builtbroken.mc.core.network.IPacketReceiver;
import com.builtbroken.mc.core.network.packet.PacketType;
import com.builtbroken.mc.lib.helper.LanguageUtility;
import icbm.classic.ICBMClassic;
import icbm.classic.prefab.item.ItemICBMElectrical;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import java.util.List;

public class ItemSignalDisrupter extends ItemICBMElectrical implements IItemFrequency, IPacketReceiver, IGuiTile
{
    public ItemSignalDisrupter()
    {
        super("signalDisrupter");
        setMaxStackSize(1);
    }

    @Override
    protected void getDetailedInfo(ItemStack stack, EntityPlayer player, List list)
    {
        super.getDetailedInfo(stack, player, list);
        list.add(LanguageUtility.getLocal("info.misc.freq") + " " + this.getBroadCastHz(stack));
    }

    @Override
    public void onUpdate(ItemStack itemStack, World world, Entity entity, int par4, boolean par5)
    {
        if (!world.isRemote)
        {
            //if (this.getEnergy(itemStack) > 20 && world.getWorldTime() % 20 == 0)
            //{
            //    this.discharge(itemStack, 1 * 20, true);
            //}
        }
    }

    @Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player)
    {
        player.openGui(ICBMClassic.INSTANCE, 10002, world, player.inventory.currentItem, 0, 0);
        return stack;
    }

    @Override
    public void read(ByteBuf buf, EntityPlayer player, PacketType packet)
    {
        int slot = buf.readInt();
        float frequency = buf.readFloat();

        ItemStack itemStack = player.inventory.getStackInSlot(slot);
        if (itemStack != null)
        {
            Item clientItem = itemStack.getItem();

            if (clientItem instanceof ItemSignalDisrupter)
            {
                ((ItemSignalDisrupter) clientItem).setBroadCastHz(itemStack, frequency);
            }
        }
    }

    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player)
    {
        return null;
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player)
    {
        return null;
    }
}
