package com.builtbroken.mc.core.network.netty;

import com.builtbroken.mc.api.data.IPacket;
import com.builtbroken.mc.core.Engine;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import net.minecraft.network.INetHandler;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;

/**
 * @author tgame14
 * @since 31/05/14
 */
@ChannelHandler.Sharable
public class ResonantPacketHandler extends SimpleChannelInboundHandler<IPacket>
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
                    packet.handleClientSide();
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
            Engine.logger().error("Failed to handle packet " + packet, e);
        }
    }

}
