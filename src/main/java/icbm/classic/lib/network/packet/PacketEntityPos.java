package icbm.classic.lib.network.packet;

import icbm.classic.ICBMClassic;
import icbm.classic.lib.network.IPacketIDReceiver;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Packet to handle updating entity position, motion, and rotation.
 * Usually after teleporting server side.
 *
 */
public class PacketEntityPos extends PacketBase<PacketEntityPos>
{
    public int entityId;
    public int world;
    public double x, y, z;
    public double mx, my, mz;
    public float yaw, pitch;

    public PacketEntityPos()
    {
        //Needed for forge to construct the packet
    }

    public PacketEntityPos(Entity entity)
    {
        this.entityId = entity.getEntityId();
        this.world = entity.world.provider.getDimension();
        this.x = entity.posX;
        this.y = entity.posY;
        this.z = entity.posZ;
        this.mx = entity.motionX;
        this.my = entity.motionY;
        this.mz = entity.motionZ;
        this.yaw = entity.rotationYaw;
        this.pitch = entity.rotationPitch;
    }

    @Override
    public void encodeInto(ChannelHandlerContext ctx, ByteBuf buffer)
    {
        buffer.writeInt(entityId);
        buffer.writeInt(world);
        buffer.writeDouble(x);
        buffer.writeDouble(y);
        buffer.writeDouble(z);
        buffer.writeDouble(mx);
        buffer.writeDouble(my);
        buffer.writeDouble(mz);
        buffer.writeFloat(yaw);
        buffer.writeFloat(pitch);
        super.encodeInto(ctx, buffer);
    }

    @Override
    public void decodeInto(ChannelHandlerContext ctx, ByteBuf buffer)
    {
        entityId = buffer.readInt();
        world = buffer.readInt();
        x = buffer.readDouble();
        y = buffer.readDouble();
        z = buffer.readDouble();
        mx = buffer.readDouble();
        my = buffer.readDouble();
        mz = buffer.readDouble();
        yaw = buffer.readFloat();
        pitch = buffer.readFloat();
        super.decodeInto(ctx, buffer);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void handleClientSide(EntityPlayer player)
    {
        if (player != null && player.world.provider.getDimension() == world)
        {
            final Entity entity = player.world.getEntityByID(entityId);
            if(entity != null) {
                entity.setPosition(x, y, z);
                entity.motionX = mx;
                entity.motionY = my;
                entity.motionZ = mz;
                entity.rotationYaw = yaw;
                entity.rotationPitch = pitch;
            }
        }
    }

    @Override
    public void handleServerSide(EntityPlayer player)
    {
    }
}
