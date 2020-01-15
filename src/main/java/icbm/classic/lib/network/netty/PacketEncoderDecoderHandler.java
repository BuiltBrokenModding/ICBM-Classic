package icbm.classic.lib.network.netty;

import icbm.classic.lib.network.IPacket;
import icbm.classic.lib.network.packet.PacketPlayerItem;
import icbm.classic.lib.network.packet.PacketRedmatterSizeSync;
import icbm.classic.lib.network.packet.PacketTile;
import icbm.classic.ICBMClassic;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraftforge.fml.common.network.FMLIndexedMessageToMessageCodec;

/**
 * @author tgame14
 * @since 31/05/14
 */
public class PacketEncoderDecoderHandler extends FMLIndexedMessageToMessageCodec<IPacket>
{
    public boolean silenceStackTrace = false; //TODO add command and config

    private int nextID = 0;

    public PacketEncoderDecoderHandler()
    {
        addPacket(PacketTile.class);
        addPacket(PacketPlayerItem.class);
        addPacket(PacketRedmatterSizeSync.class);
    }

    public void addPacket(Class<? extends IPacket> clazz)
    {
        addDiscriminator(nextID++, clazz);
    }

    @Override
    public void encodeInto(ChannelHandlerContext ctx, IPacket packet, ByteBuf target) throws Exception
    {
        try
        {
            packet.encodeInto(ctx, target);
        }
        catch (Exception e)
        {
            if (!silenceStackTrace)
            {
                ICBMClassic.logger().error("Failed to encode packet " + packet, e);
            }
            else
            {
                ICBMClassic.logger().error("Failed to encode packet " + packet + " E: " + e.getMessage());
            }
        }
    }

    @Override
    public void decodeInto(ChannelHandlerContext ctx, ByteBuf source, IPacket packet)
    {
        try
        {
            packet.decodeInto(ctx, source);
        }
        catch (Exception e)
        {
            if (!silenceStackTrace)
            {
                ICBMClassic.logger().error("Failed to decode packet " + packet, e);
            }
            else
            {
                ICBMClassic.logger().error("Failed to decode packet " + packet + " E: " + e.getMessage());
            }
        }
    }
}
