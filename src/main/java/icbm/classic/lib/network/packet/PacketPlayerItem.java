package icbm.classic.lib.network.packet;

import icbm.classic.lib.network.IPacketIDReceiver;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * @author tgame14
 * @since 26/05/14
 */
@Deprecated
public class PacketPlayerItem extends PacketBase<PacketPlayerItem>
{
    public int slotId;
    public int id = 0;

    public PacketPlayerItem()
    {
        //Needed to construct the packet
    }

    public PacketPlayerItem(int slotId)
    {
        this.slotId = slotId;
    }

    public PacketPlayerItem(Player player)
    {
        this(player.getInventory().selected);
    }

    @Override
    public void encodeInto(RegistryFriendlyByteBuf buffer)
    {
        buffer.writeInt(slotId);
        buffer.writeInt(id);
        super.encodeInto(buffer);
    }

    @Override
    public void decodeInto(RegistryFriendlyByteBuf buffer)
    {
        slotId = buffer.readInt();
        id = buffer.readInt();
        super.decodeInto(buffer);
    }

    @Override
    public void handleClientSide(Minecraft minecraft, Player player)
    {
        handleServerSide(player);
    }

    @Override
    public void handleServerSide(Player player)
    {
        if (slotId < 0)
        {
            final Item item = BuiltInRegistries.ITEM.byId(Math.abs(this.slotId));
            if (item instanceof IPacketIDReceiver)
            {
                ((IPacketIDReceiver) item).read(data(), id, player, this);
            }
        }
        else
        {
            final ItemStack stack = player.getInventory().getItem(this.slotId);
            if (stack.getItem() instanceof IPacketIDReceiver)
            {
                ((IPacketIDReceiver) stack.getItem()).read(data(), id, player, this);
            }
        }
    }
}
