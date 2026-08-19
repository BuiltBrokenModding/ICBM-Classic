package icbm.classic.lib.network.packet;

import icbm.classic.ICBMClassic;
import icbm.classic.lib.network.netty.PacketManager;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class PacketSpawnAirParticle {
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

    public void encode(PacketBuffer buffer)
    {
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

    public static PacketSpawnAirParticle decode(PacketBuffer buffer)
    {
        final PacketSpawnAirParticle rv = new PacketSpawnAirParticle();
        rv.posX = buffer.readDouble();
        rv.posY = buffer.readDouble();
        rv.posZ = buffer.readDouble();
        rv.v = buffer.readDouble();
        rv.v1 = buffer.readDouble();
        rv.v2 = buffer.readDouble();
        rv.red = buffer.readFloat();
        rv.green = buffer.readFloat();
        rv.blue = buffer.readFloat();
        rv.scale = buffer.readFloat();
        rv.ticksToLive = buffer.readInt();
        return rv;
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        final NetworkEvent.Context context = contextSupplier.get();
        if (context.getDirection().getReceptionSide().isClient()) {
            context.setPacketHandled(true);
            ICBMClassic.proxy.spawnAirParticle(Minecraft.getInstance().world,
                this.posX, this.posY, this.posZ, this.v, this.v1, this.v2,
                this.red, this.green, this.blue, this.scale, this.ticksToLive
            );
        } else {
            ICBMClassic.logger().warn(String.format("Received %s packet serverside when we expected it clientside instead", this.getClass()));
        }
    }

    public static void sendToAllClientsInWorld(World world, double x, double y, double z, double v, double v1, double v2,
                                               float red, float green, float blue, float scale, int ticksToLive) {
        PacketManager.sendToAllInDimension(new PacketSpawnAirParticle(x, y, z, v, v1, v2, red, green, blue, scale, ticksToLive), world);
    }
}
