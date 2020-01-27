package icbm.classic.lib.network.packet;

import icbm.classic.content.blast.BlastRedmatter;
import icbm.classic.content.entity.EntityExplosion;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class PacketRedmatterSizeSync extends PacketBase<PacketRedmatterSizeSync>
{
    public float size;
    public int id;

    public PacketRedmatterSizeSync()
    {
        //Needed for forge to construct the packet
    }

    /**
     * @param size Redmatter size
     * @param id The redmatter entity's id
     */
    public PacketRedmatterSizeSync(float size, int id)
    {
        this.size = size;
        this.id = id;
    }

    @Override
    public void encodeInto(ChannelHandlerContext ctx, ByteBuf buffer)
    {
        buffer.writeFloat(size);
        ByteBufUtils.writeVarInt(buffer, id, 5);
        super.encodeInto(ctx, buffer);
    }

    @Override
    public void decodeInto(ChannelHandlerContext ctx, ByteBuf buffer)
    {
        size = buffer.readFloat();
        id = ByteBufUtils.readVarInt(buffer, 5);
        super.decodeInto(ctx, buffer);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void handleClientSide(EntityPlayer player)
    {
        if (player != null)
        {
            Entity entity = Minecraft.getMinecraft().world.getEntityByID(id);

            if(entity instanceof EntityExplosion && ((EntityExplosion)entity).getBlast() instanceof BlastRedmatter)
                ((BlastRedmatter)((EntityExplosion)entity).getBlast()).targetSize = size;
        }
    }
}
