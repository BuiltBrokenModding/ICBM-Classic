package icbm.classic.lib.network.packet;

import java.util.List;
import icbm.classic.content.blast.BlastRedmatter;
import icbm.classic.content.entity.EntityExplosion;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class PacketRedmatterSizeSync extends PacketBase<PacketRedmatterSizeSync>
{
    public float size;
    public BlockPos pos;

    public PacketRedmatterSizeSync()
    {
        //Needed for forge to construct the packet
    }

    /**
     * @param size Redmatter size
     * @param pos The redmatter's position
     */
    public PacketRedmatterSizeSync(float size, BlockPos pos)
    {
        this.size = size;
        this.pos = pos;
    }

    @Override
    public void encodeInto(ChannelHandlerContext ctx, ByteBuf buffer)
    {
        buffer.writeFloat(size);
        buffer.writeLong(pos.toLong());
        super.encodeInto(ctx, buffer);
    }

    @Override
    public void decodeInto(ChannelHandlerContext ctx, ByteBuf buffer)
    {
        size = buffer.readFloat();
        pos = BlockPos.fromLong(buffer.readLong());
        super.decodeInto(ctx, buffer);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void handleClientSide(EntityPlayer player)
    {
        if (player != null)
        {
            List<EntityExplosion> entityList = Minecraft.getMinecraft().world.getEntitiesWithinAABB(EntityExplosion.class, new AxisAlignedBB(pos));

            if(entityList.size() > 0 && entityList.get(0).getBlast() instanceof BlastRedmatter)
                ((BlastRedmatter)entityList.get(0).getBlast()).targetSize = size;
        }
    }
}
