package icbm.classic.lib.network.packet;

import icbm.classic.lib.network.IPacketIDReceiver;
import icbm.classic.lib.network.ex.PacketTileReadException;
import icbm.classic.ICBMClassic;
import icbm.classic.lib.transform.vector.Location;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Packet type designed to be used with Tiles
 *
 * @author tgame14
 * @since 26/05/14
 */
public class PacketTile extends PacketBase<PacketTile>
{
    public int x;
    public int y;
    public int z;
    public int id;

    public String name;

    public PacketTile()
    {
        //Needed for forge to construct the packet
    }

    /**
     * @param x - location
     * @param y - location
     * @param z - location
     */
    public PacketTile(String name, int id, int x, int y, int z)
    {
        this.name = name;
        this.id = id;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /**
     * @param tile - TileEntity to send this packet to, only used for location data
     */
    public PacketTile(String name, int id, TileEntity tile)
    {
        this(name, id, tile.getPos().getX(), tile.getPos().getY(), tile.getPos().getZ());
    }

    @Override
    public void encodeInto(ChannelHandlerContext ctx, ByteBuf buffer)
    {
        buffer.writeBoolean(ICBMClassic.runningAsDev);
        if (ICBMClassic.runningAsDev)
        {
            ByteBufUtils.writeUTF8String(buffer, name);
        }
        buffer.writeInt(id);
        buffer.writeInt(x);
        buffer.writeInt(y);
        buffer.writeInt(z);
        super.encodeInto(ctx, buffer);
    }

    @Override
    public void decodeInto(ChannelHandlerContext ctx, ByteBuf buffer)
    {
        if (buffer.readBoolean())
        {
            name = ByteBufUtils.readUTF8String(buffer);
        }
        id = buffer.readInt();
        x = buffer.readInt();
        y = buffer.readInt();
        z = buffer.readInt();
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
    public void handle(EntityPlayer player)
    {
        if (player.getEntityWorld() == null)
        {
            if (ICBMClassic.runningAsDev)
            {
                ICBMClassic.logger().error("PacketTile#handle(" + player + ") - world is null for player while handling packet. ", new RuntimeException());
            }
            return;
        }
        final BlockPos pos = new BlockPos(this.x, this.y, this.z);
        if (player.getEntityWorld().isBlockLoaded(pos))
        {
            handle(player, player.getEntityWorld().getTileEntity(pos));
        }
        else if (ICBMClassic.runningAsDev)
        {
            ICBMClassic.logger().error("PacketTile#handle(" + player + ") - block is not loaded for player while handling packet. ", new RuntimeException());
        }
    }

    /**
     * Called to handler a packet when it is received
     *
     * @param player - player who received the packet
     * @param tile   - tile who is receiving the packet
     */
    public void handle(EntityPlayer player, TileEntity tile)
    {
        //TODO add checksum or hash to verify the packet is sent to the correct tile
        final Location location = new Location(player.world, x, y, z);
        if (tile == null)
        {
            ICBMClassic.logger().error(new PacketTileReadException(location, "Null tile"));
        }
        else if (tile.isInvalid())
        {
            ICBMClassic.logger().error(new PacketTileReadException(location, "Invalidated tile"));
        }
        else if (tile instanceof IPacketIDReceiver)
        {
            if (((IPacketIDReceiver) tile).shouldReadPacket(player, location, this))
            {
                try
                {
                    IPacketIDReceiver receiver = (IPacketIDReceiver) tile;
                    receiver.read(dataToRead, id, player, this);
                }
                catch (IndexOutOfBoundsException e)
                {
                    ICBMClassic.logger().error(new PacketTileReadException(location, "Packet was read past its size."));
                    ICBMClassic.logger().error("Error: ", e);
                }
                catch (NullPointerException e)
                {
                    ICBMClassic.logger().error(new PacketTileReadException(location, "Null pointer while reading data", e));
                    ICBMClassic.logger().error("Error: ", e);
                }
                catch (Exception e)
                {
                    ICBMClassic.logger().error(new PacketTileReadException(location, "Failed to read packet", e));
                    ICBMClassic.logger().error("Error: ", e);
                }
            }
            else
            {
                ICBMClassic.logger().error("Error: " + tile + " rejected packet " + this + " due to invalid conditions.");
            }
        }
        else
        {
            ICBMClassic.logger().error(new PacketTileReadException(location, "Unsupported action for " + tile));
        }
    }
}
