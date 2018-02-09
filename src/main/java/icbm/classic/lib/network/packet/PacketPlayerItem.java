package icbm.classic.lib.network.packet;

import icbm.classic.lib.network.IPacketIDReceiver;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

/**
 * @author tgame14
 * @since 26/05/14
 */
public class PacketPlayerItem extends PacketBase<PacketPlayerItem>
{
    public int slotId;
    public int id = 0;

    public PacketPlayerItem()
    {
        //Needed for forge to construct the packet
    }

    public PacketPlayerItem(int slotId)
    {
        this.slotId = slotId;
    }

    public PacketPlayerItem(EntityPlayer player)
    {
        this(player.inventory.currentItem);
    }

    @Override
    public void encodeInto(ChannelHandlerContext ctx, ByteBuf buffer)
    {
        buffer.writeInt(slotId);
        buffer.writeInt(id);
        super.encodeInto(ctx, buffer);
    }

    @Override
    public void decodeInto(ChannelHandlerContext ctx, ByteBuf buffer)
    {
        slotId = buffer.readInt();
        id = buffer.readInt();
        super.decodeInto(ctx, buffer);
    }

    @Override
    public void handleClientSide(EntityPlayer player)
    {
        handleServerSide(player);
    }

    @Override
    public void handleServerSide(EntityPlayer player)
    {
        if (slotId < 0)
        {
            final Item item = Item.getItemById(Math.abs(this.slotId));
            if (item != null)
            {
                if (item instanceof IPacketIDReceiver)
                {
                    ((IPacketIDReceiver) item).read(data(), id, player, this);
                }
            }
        }
        else
        {
            final ItemStack stack = player.inventory.getStackInSlot(this.slotId);
            if (stack.getItem() instanceof IPacketIDReceiver)
            {
                ((IPacketIDReceiver) stack.getItem()).read(data(), id, player, this);
            }
        }
    }
}
