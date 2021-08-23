package icbm.classic.lib.network.packet;

import icbm.classic.ICBMClassic;
import icbm.classic.content.blast.BlastRedmatter;
import icbm.classic.content.entity.EntityExplosion;
import icbm.classic.lib.network.IPacket;
import icbm.classic.lib.transform.vector.Pos;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PacketSpawnAirParticle implements IPacket<PacketSpawnAirParticle>
{
    /*
    Id of the dimension that this particle should be placed in.
     */
    private int dimId;

    // x y and z positions
    private double posX;
    private double posY;
    private double posZ;

    // x y and z velocities
    private double v;
    private double v1;
    private double v2;

    // red green and blue color values
    private float red;
    private float green;
    private float blue;

    // size scale
    private float scale;

    // how long this particle will live for
    private int ticksToLive;

    public PacketSpawnAirParticle()
    {
        //Needed for forge to construct the packet
    }

    public PacketSpawnAirParticle(int dimId, double posX, double posY, double posZ, double v, double v1, double v2,
                                  float red, float green, float blue, float scale, int ticksToLive)
    {
        this.dimId = dimId;
        this.posX = posX;
        this.posY = posY;
        this.posZ = posZ;
        this.v = v;
        this.v1 = v1;
        this.v2 = v2;
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.scale = scale;
        this.ticksToLive = ticksToLive;
    }

    @Override
    public void encodeInto(ChannelHandlerContext ctx, ByteBuf buffer)
    {
        buffer.writeInt(dimId);
        buffer.writeDouble(posX);
        buffer.writeDouble(posY);
        buffer.writeDouble(posZ);
        buffer.writeDouble(v);
        buffer.writeDouble(v1);
        buffer.writeDouble(v2);
        buffer.writeFloat(red);
        buffer.writeFloat(green);
        buffer.writeFloat(blue);
        buffer.writeFloat(scale);
        buffer.writeInt(ticksToLive);
    }

    @Override
    public void decodeInto(ChannelHandlerContext ctx, ByteBuf buffer)
    {
        dimId = buffer.readInt();
        posX = buffer.readDouble();
        posY = buffer.readDouble();
        posZ = buffer.readDouble();
        v = buffer.readDouble();
        v1 = buffer.readDouble();
        v2 = buffer.readDouble();
        red = buffer.readFloat();
        green = buffer.readFloat();
        blue = buffer.readFloat();
        scale = buffer.readFloat();
        ticksToLive = buffer.readInt();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void handleClientSide(EntityPlayer player)
    {
        if (Minecraft.getMinecraft().world != null && player.world.provider.getDimension() == dimId)
        {
            ICBMClassic.proxy.spawnAirParticle(player.world, new Pos(posX, posY, posZ), v, v1, v2, red, green, blue, scale, ticksToLive);
        }
    }

    public static void sendToAllClients(World world, Pos position, double v, double v1, double v2, float red, float green, float blue, float scale, int ticksToLive)
    {
        final int dimid = world.provider.getDimension();
        final PacketSpawnAirParticle packet = new PacketSpawnAirParticle(dimid, position.x(), position.y(), position.z(), v, v1, v2, red, green, blue, scale, ticksToLive);
        final NetworkRegistry.TargetPoint point = new NetworkRegistry.TargetPoint(dimid, position.x(), position.y(), position.z(), 256);
        ICBMClassic.packetHandler.sendToAllAround(packet, point);
    }
}
