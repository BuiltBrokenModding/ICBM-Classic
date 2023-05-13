package icbm.classic.lib.network.packet;

import icbm.classic.ICBMClassic;
import icbm.classic.lib.network.IPacketIDReceiver;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Packet type designed to be used with Tiles
 *
 * @author tgame14
 * @since 26/05/14
 */
public class PacketEntity extends PacketBase<PacketEntity>
{
    public int entityId;
    public int id;

    public String name;

    public PacketEntity()
    {
        //Needed for forge to construct the packet
    }

    public PacketEntity(String name, int entityId, int id)
    {
        this.name = name;
        this.entityId = entityId;
        this.id = id;
    }

    @Override
    public void encodeInto(ChannelHandlerContext ctx, ByteBuf buffer)
    {
        buffer.writeBoolean(ICBMClassic.runningAsDev);
        if (ICBMClassic.runningAsDev)
        {
            ByteBufUtils.writeUTF8String(buffer, name);
        }
        buffer.writeInt(entityId);
        buffer.writeInt(id);
        super.encodeInto(ctx, buffer);
    }

    @Override
    public void decodeInto(ChannelHandlerContext ctx, ByteBuf buffer)
    {
        if (buffer.readBoolean())
        {
            name = ByteBufUtils.readUTF8String(buffer);
        }
        entityId = buffer.readInt();
        id = buffer.readInt();
        super.decodeInto(ctx, buffer);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void handleClientSide(EntityPlayer player)
    {
        if (player != null)
        {
            handle(player);
        }
        else if (ICBMClassic.runningAsDev)
        {
            ICBMClassic.logger().error("PacketTile#handleClientSide(null) - player was null for packet", new RuntimeException());
        }
    }

    @Override
    public void handleServerSide(EntityPlayer player)
    {
        if (player != null)
        {
            handle(player);
        }
        else if (ICBMClassic.runningAsDev)
        {
            ICBMClassic.logger().error("PacketTile#handleServerSide(null) - player was null for packet", new RuntimeException());
        }
    }

    /**
     * Called to handle a packet when it is received
     *
     * @param player - player who received the packet
     */
    public void handle(EntityPlayer player) {
        if (player.getEntityWorld() == null) {
            if (ICBMClassic.runningAsDev) {
                ICBMClassic.logger().error("PacketTile#handle(" + player + ") - world is null for player while handling packet. ", new RuntimeException());
            }
            return;
        }
        handle(player, player.getEntityWorld().getEntityByID(entityId));
    }

    /**
     * Called to handler a packet when it is received
     *
     * @param player received the packet
     * @param entity receiving the packet
     */
    public void handle(EntityPlayer player, Entity entity)
    {
        if (entity == null)
        {
            ICBMClassic.logger().error("Couldn't match entity for entityId= " + entityId + " ID=" + id + " name=" + name);
        }
        else if (entity instanceof IPacketIDReceiver)
        {
            IPacketIDReceiver receiver = (IPacketIDReceiver) entity;
            if (receiver.shouldReadPacket(player, null, this))
            {
                try
                {
                    receiver.read(dataToRead, id, player, this);
                }
                catch (IndexOutOfBoundsException e)
                {
                    ICBMClassic.logger().error(entity + ": Packet was read past its size. ID=" + id + " name=" + name);
                    ICBMClassic.logger().error("Error: ", e);
                }
                catch (NullPointerException e)
                {
                    ICBMClassic.logger().error(entity + ": Null pointer while reading data. ID=" + id + " name=" + name);
                    ICBMClassic.logger().error("Error: ", e);
                }
                catch (Exception e)
                {
                    ICBMClassic.logger().error(entity + ": Failed to read packet. ID=" + id + " name=" + name);
                    ICBMClassic.logger().error("Error: ", e);
                }
            }
            else
            {
                ICBMClassic.logger().error("Error: " + entity + " rejected packet " + this + " due to invalid conditions.");
            }
        }
        else
        {
            ICBMClassic.logger().error("Error: " + entity + " doesn't implement IPacketIDReceiver");
        }
    }
}
