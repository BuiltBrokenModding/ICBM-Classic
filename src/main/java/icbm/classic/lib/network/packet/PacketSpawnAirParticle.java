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

public class PacketSpawnAirParticle implements IPacket<PacketSpawnAirParticle>
{
    /*
    Id of the dimension that this particle should be placed in.
     */
    private ResourceKey<Level> dimId;

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

    public PacketSpawnAirParticle(ResourceKey<Level> dimId, double posX, double posY, double posZ, double v, double v1, double v2,
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
    public void encodeInto(RegistryFriendlyByteBuf buffer)
    {
        buffer.writeResourceKey(dimId);
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
    public void decodeInto(RegistryFriendlyByteBuf buffer)
    {
        dimId = buffer.readResourceKey(Registries.DIMENSION);
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
    public void handleClientSide(Minecraft minecraft, Player player)
    {
        if (minecraft.level != null && player.level().dimension().equals(dimId))
        {
            ICBMClassic.proxy.spawnAirParticle(player.level(), posX, posY, posZ, v, v1, v2, red, green, blue, scale, ticksToLive);
        }
    }

    public static void sendToAllClients(Level world, double x, double y, double z, double v, double v1, double v2, float red, float green, float blue, float scale, int ticksToLive)
    {
        final ResourceKey<Level> dimid = world.dimension();
        final PacketSpawnAirParticle packet = new PacketSpawnAirParticle(dimid, x, y, z, v, v1, v2, red, green, blue, scale, ticksToLive);
        final TargetPoint point = new TargetPoint(dimid, x, y, z, 256);
        ICBMClassic.packetHandler.sendToAllAround(packet, point);
    }
}
