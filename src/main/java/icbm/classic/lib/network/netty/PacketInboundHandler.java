package icbm.classic.lib.network.netty;

import icbm.classic.ICBMClassic;
import icbm.classic.lib.network.IPacket;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.network.INetHandler;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * @author tgame14
 * @since 31/05/14
 */
@ChannelHandler.Sharable
public class PacketInboundHandler extends SimpleChannelInboundHandler<IPacket>
{
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, IPacket packet) throws Exception
    {
        try
        {
            INetHandler netHandler = ctx.channel().attr(NetworkRegistry.NET_HANDLER).get();

            switch (FMLCommonHandler.instance().getEffectiveSide())
            {
                case CLIENT:
                    handleClientSide(packet);
                    break;
                case SERVER:
                    packet.handleServerSide(((NetHandlerPlayServer) netHandler).player);
                    break;
                default:
                    break;
            }
        }
        catch (Exception e)
        {
            ICBMClassic.logger().error("Failed to handle packet " + packet, e);
        }
    }

    @SideOnly(Side.CLIENT)
    private void handleClientSide(IPacket packet) {
        packet.handleClientSide(Minecraft.getMinecraft(), Minecraft.getMinecraft().player);
    }
}
