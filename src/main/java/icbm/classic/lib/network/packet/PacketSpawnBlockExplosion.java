package icbm.classic.lib.network.packet;

import icbm.classic.ICBMClassic;
import icbm.classic.lib.network.IPacket;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class PacketSpawnBlockExplosion implements IPacket<PacketSpawnBlockExplosion>
{
    /*
    Id of the dimension that this particle should be placed in.
     */
    private int dimId;

    // x y and z positions
    private double sourceX;
    private double sourceY;
    private double sourceZ;

    private double blastScale;

    private BlockPos blockPos;

    public PacketSpawnBlockExplosion()
    {
        //Needed for forge to construct the packet
    }

    public PacketSpawnBlockExplosion(int dimId, double sourceX, double sourceY, double sourceZ, double blastScale, BlockPos pos)
    {
        this.dimId = dimId;
        this.sourceX = sourceX;
        this.sourceY = sourceY;
        this.sourceZ = sourceZ;
        this.blockPos = pos;
        this.blastScale = blastScale;
    }

    @Override
    public void encodeInto(ChannelHandlerContext ctx, ByteBuf buffer)
    {
        buffer.writeInt(dimId);
        buffer.writeDouble(sourceX);
        buffer.writeDouble(sourceY);
        buffer.writeDouble(sourceZ);
        buffer.writeDouble(blastScale);
        buffer.writeInt(blockPos.getX());
        buffer.writeInt(blockPos.getY());
        buffer.writeInt(blockPos.getZ());
    }

    @Override
    public void decodeInto(ChannelHandlerContext ctx, ByteBuf buffer)
    {
        dimId = buffer.readInt();
        sourceX = buffer.readDouble();
        sourceY = buffer.readDouble();
        sourceZ = buffer.readDouble();
        blastScale = buffer.readDouble();
        blockPos = new BlockPos(buffer.readInt(), buffer.readInt(), buffer.readInt());
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void handleClientSide(Minecraft minecraft, EntityPlayer player)
    {
        if (minecraft.world != null && player.world.provider.getDimension() == dimId)
        {
            ICBMClassic.proxy.spawnExplosionParticles(player.world, sourceX, sourceY, sourceZ, blastScale, blockPos);
        }
    }

    public static void sendToAllClients(World world, double sourceX, double sourceY, double sourceZ, double blastScale, BlockPos pos)
    {
        final int dimid = world.provider.getDimension();
        final PacketSpawnBlockExplosion packet = new PacketSpawnBlockExplosion(dimid, sourceX, sourceY, sourceZ, blastScale, pos);
        final NetworkRegistry.TargetPoint point = new NetworkRegistry.TargetPoint(dimid, pos.getX(), pos.getY(), pos.getZ(), 256);
        ICBMClassic.packetHandler.sendToAllAround(packet, point);
    }
}
