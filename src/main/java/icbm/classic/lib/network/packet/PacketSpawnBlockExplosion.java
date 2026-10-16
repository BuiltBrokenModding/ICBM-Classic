package icbm.classic.lib.network.packet;

import icbm.classic.ICBMClassic;
import icbm.classic.lib.network.IPacket;
import icbm.classic.lib.network.TargetPoint;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class PacketSpawnBlockExplosion implements IPacket<PacketSpawnBlockExplosion>
{
    /*
    Id of the dimension that this particle should be placed in.
     */
    private ResourceKey<Level> dimId;

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

    public PacketSpawnBlockExplosion(ResourceKey<Level> dimId, double sourceX, double sourceY, double sourceZ, double blastScale, BlockPos pos)
    {
        this.dimId = dimId;
        this.sourceX = sourceX;
        this.sourceY = sourceY;
        this.sourceZ = sourceZ;
        this.blockPos = pos;
        this.blastScale = blastScale;
    }

    @Override
    public void encodeInto(RegistryFriendlyByteBuf buffer)
    {
        buffer.writeResourceKey(dimId);
        buffer.writeDouble(sourceX);
        buffer.writeDouble(sourceY);
        buffer.writeDouble(sourceZ);
        buffer.writeDouble(blastScale);
        buffer.writeInt(blockPos.getX());
        buffer.writeInt(blockPos.getY());
        buffer.writeInt(blockPos.getZ());
    }

    @Override
    public void decodeInto(RegistryFriendlyByteBuf buffer)
    {
        dimId = buffer.readResourceKey(Registries.DIMENSION);
        sourceX = buffer.readDouble();
        sourceY = buffer.readDouble();
        sourceZ = buffer.readDouble();
        blastScale = buffer.readDouble();
        blockPos = new BlockPos(buffer.readInt(), buffer.readInt(), buffer.readInt());
    }

    @Override
    public void handleClientSide(Minecraft minecraft, Player player)
    {
        if (minecraft.level != null && player.level().dimension().equals(dimId))
        {
            ICBMClassic.proxy.spawnExplosionParticles(player.level(), sourceX, sourceY, sourceZ, blastScale, blockPos);
        }
    }

    public static void sendToAllClients(Level world, double sourceX, double sourceY, double sourceZ, double blastScale, BlockPos pos)
    {
        final ResourceKey<Level> dimid = world.dimension();
        final PacketSpawnBlockExplosion packet = new PacketSpawnBlockExplosion(dimid, sourceX, sourceY, sourceZ, blastScale, pos);
        final TargetPoint point = new TargetPoint(dimid, pos.getX(), pos.getY(), pos.getZ(), 256);
        ICBMClassic.packetHandler.sendToAllAround(packet, point);
    }
}
