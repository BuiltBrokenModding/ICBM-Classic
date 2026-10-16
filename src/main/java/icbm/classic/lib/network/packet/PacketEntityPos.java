package icbm.classic.lib.network.packet;

import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * Packet to handle updating entity position, motion, and rotation.
 * Usually after teleporting server side.
 *
 */
@Deprecated
public class PacketEntityPos extends PacketBase<PacketEntityPos>
{
    public int entityId;
    public ResourceKey<Level> world;
    public double x, y, z;
    public double mx, my, mz;
    public float yaw, pitch;

    public PacketEntityPos()
    {
        //Needed for forge to construct the packet
    }

    public PacketEntityPos(Entity entity)
    {
        this.entityId = entity.getId();
        this.world = entity.level().dimension();
        this.x = entity.getX();
        this.y = entity.getY();
        this.z = entity.getZ();
        this.mx = entity.getDeltaMovement().x;
        this.my = entity.getDeltaMovement().y;
        this.mz = entity.getDeltaMovement().z;
        this.yaw = entity.getYRot();
        this.pitch = entity.getXRot();
    }

    @Override
    public void encodeInto(RegistryFriendlyByteBuf buffer)
    {
        buffer.writeInt(entityId);
        buffer.writeResourceKey(world);
        buffer.writeDouble(x);
        buffer.writeDouble(y);
        buffer.writeDouble(z);
        buffer.writeDouble(mx);
        buffer.writeDouble(my);
        buffer.writeDouble(mz);
        buffer.writeFloat(yaw);
        buffer.writeFloat(pitch);
        super.encodeInto(buffer);
    }

    @Override
    public void decodeInto(RegistryFriendlyByteBuf buffer)
    {
        entityId = buffer.readInt();
        world = buffer.readResourceKey(Registries.DIMENSION);
        x = buffer.readDouble();
        y = buffer.readDouble();
        z = buffer.readDouble();
        mx = buffer.readDouble();
        my = buffer.readDouble();
        mz = buffer.readDouble();
        yaw = buffer.readFloat();
        pitch = buffer.readFloat();
        super.decodeInto(buffer);
    }

    @Override
    public void handleClientSide(Minecraft minecraft, Player player)
    {
        if (player != null && player.level().dimension().equals(world))
        {
            final Entity entity = player.level().getEntity(entityId);
            if(entity != null) {
                entity.setPos(x, y, z);
                entity.setDeltaMovement(mx, my, mz);
                entity.setYRot(yaw);
                entity.setXRot(pitch);
            }
        }
    }

    @Override
    public void handleServerSide(Player player)
    {
    }
}
