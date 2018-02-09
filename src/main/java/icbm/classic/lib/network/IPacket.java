package icbm.classic.lib.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Base object for all custom packets using in VoltzEngine.
 * </p>
 * Ensure that there is always a default constructor so forge can create the packet.
 * <p>
 * <p>
 * An NPE will be thrown if the packet is not registered due to PacketManager not knowing how to handle it
 * <p>
 * See PacketManager#sendToAll(IPacket) for exact usage on sending the packet
 *
 * @author tgame14, DarkCow
 * @since 26/05/14
 */
public interface IPacket<P extends IPacket>
{

    /**
     * Encode the packet data into the ByteBuf stream. Complex data sets may need specific data handlers
     *
     * @param ctx    channel context
     * @param buffer the buffer to encode into
     *               PacketManager#writeData(io.netty.buffer.ByteBuf, Object...)
     */
    void encodeInto(ChannelHandlerContext ctx, ByteBuf buffer);

    /**
     * Decode the packet data from the ByteBuf stream. Complex data sets may need specific data handlers
     *
     * @param ctx    channel context
     * @param buffer the buffer to decode from
     */
    void decodeInto(ChannelHandlerContext ctx, ByteBuf buffer);

    default P addData(Object... objects)
    {
        return (P) this;
    }

    /**
     * Handle a packet on the client side. Note this occurs after decoding has completed.
     */
    @SideOnly(Side.CLIENT)
    default void handleClientSide()
    {
        handleClientSide((EntityPlayer)Minecraft.getMinecraft().player);
    }

    default void handleClientSide(EntityPlayer player)
    {
        throw new UnsupportedOperationException("Unsupported operation for Packet: " + getClass().getSimpleName());
    }

    /**
     * Handle a packet on the server side. Note this occurs after decoding has completed.
     *
     * @param player the player reference
     */
    default void handleServerSide(EntityPlayer player)
    {
        throw new UnsupportedOperationException("Unsupported operation for Packet: " + getClass().getSimpleName());
    }
}
