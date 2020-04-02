package icbm.classic.lib.network.packet;

import icbm.classic.ICBMClassic;
import icbm.classic.content.blast.BlastRedmatter;
import icbm.classic.content.entity.EntityExplosion;
import icbm.classic.lib.network.IPacket;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class PacketRedmatterSizeSync implements IPacket<PacketRedmatterSizeSync> //TODO replace with entity data manager
{
    /** Size of the blast */
    public float size;
    /** ID of the entity controller of the blast */
    public int entityID;

    public PacketRedmatterSizeSync()
    {
        //Needed for forge to construct the packet
    }

    public PacketRedmatterSizeSync(float size, int entityID)
    {
        this.size = size;
        this.entityID = entityID;
    }

    @Override
    public void encodeInto(ChannelHandlerContext ctx, ByteBuf buffer)
    {
        buffer.writeFloat(size);
        ByteBufUtils.writeVarInt(buffer, entityID, 5);
    }

    @Override
    public void decodeInto(ChannelHandlerContext ctx, ByteBuf buffer)
    {
        size = buffer.readFloat();
        entityID = ByteBufUtils.readVarInt(buffer, 5);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void handleClientSide(EntityPlayer player)
    {
        if (Minecraft.getMinecraft().world != null)
        {
            final Entity entity = Minecraft.getMinecraft().world.getEntityByID(entityID);
            if (entity instanceof EntityExplosion && ((EntityExplosion) entity).getBlast() instanceof BlastRedmatter)
            {
                ((BlastRedmatter) ((EntityExplosion) entity).getBlast()).size = size;
            }
        }
    }

    public static void sync(BlastRedmatter redmatter)
    {
        final PacketRedmatterSizeSync packet = new PacketRedmatterSizeSync(redmatter.size, redmatter.controller.getEntityId());
        final NetworkRegistry.TargetPoint point = new NetworkRegistry.TargetPoint(redmatter.world.provider.getDimension(),
                redmatter.x, redmatter.y, redmatter.z,
                256);
        ICBMClassic.packetHandler.sendToAllAround(packet, point);
    }
}
