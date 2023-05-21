package icbm.classic.lib.network.lambda.tile;

import icbm.classic.lib.network.IPacket;
import icbm.classic.lib.network.PacketEvents;
import icbm.classic.lib.network.lambda.PacketCodex;
import icbm.classic.lib.network.lambda.PacketCodexReg;
import icbm.classic.lib.tracker.EventTrackerHelpers;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;
import java.util.function.Consumer;

@Data
@NoArgsConstructor
public class PacketLambdaTile<TARGET> implements IPacket<PacketLambdaTile<TARGET>> {

    private PacketCodex codex;
    private Integer dimensionId;
    private BlockPos pos;
    private List<Consumer<ByteBuf>> writers;
    private List<Consumer<TARGET>> setters;

    public PacketLambdaTile(PacketCodexTile codex, TileEntity tile, TARGET target) {
        this(codex, tile.getWorld(), tile.getPos(), target);
    }

    public PacketLambdaTile(PacketCodexTile codex, World dimensionId, BlockPos pos, TARGET target) {
      this(codex, dimensionId, pos.getX(), pos.getY(), pos.getZ(), target);
    }

    public PacketLambdaTile(PacketCodexTile codex, World dimensionId, int x, int y, int z, TARGET target) {
        this.codex = codex;

        setDimensionId(dimensionId.provider.getDimension());
        setPos(new BlockPos(x, y, z));

        writers = codex.encodeAsWriters(target);
    }

    @Override
    public void encodeInto(ChannelHandlerContext ctx, ByteBuf buffer) {
        // Write general data
        buffer.writeInt(codex.getId());
        buffer.writeInt(dimensionId);
        buffer.writeInt(pos.getX());
        buffer.writeInt(pos.getY());
        buffer.writeInt(pos.getZ());

        // Write data from builder
        writers.forEach(c -> c.accept(buffer));
    }

    @Override
    public void decodeInto(ChannelHandlerContext ctx, ByteBuf buffer) {

        // Read general data
        codex = PacketCodexReg.get(buffer.readInt());
        dimensionId = buffer.readInt();
        pos = new BlockPos(buffer.readInt(), buffer.readInt(), buffer.readInt());

        // Read data for builder
        setters = codex.decodeAsSetters(buffer);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void handleClientSide(final Minecraft minecraft, final EntityPlayer player)
    {
        final int playerDim = player.world.provider.getDimension();

        // Normal, player may have changed dim between network calls
        if (playerDim != getDimensionId()) {
            PacketEvents.onWrongWorld(codex, EventTrackerHelpers.SIDE_CLIENT, getDimensionId(), playerDim);
            return;
        }

        final World world = player.world;

        minecraft.addScheduledTask(() -> loadDataIntoTile(world, player));
    }

    @Override
    public void handleServerSide(EntityPlayer player)
    {
        final int playerDim = player.world.provider.getDimension();

        // Normal, player may have changed dim between network calls
        if (playerDim != getDimensionId()) {
            PacketEvents.onWrongWorld(codex, EventTrackerHelpers.SIDE_SERVER, getDimensionId(), playerDim);
            return;
        }

        // Issue, should never happen
        if(!(player.world instanceof WorldServer)) {
            PacketEvents.onNotServerWorld(codex, getDimensionId());
            return;
        }

        final WorldServer world = (WorldServer) player.world;
        world.addScheduledTask(() -> loadDataIntoTile(world, player));
    }

    private void loadDataIntoTile(World world, EntityPlayer player) {

        // Area is no longer loaded, this is normal in most cases
        if(!world.isBlockLoaded(pos)) {
            return;
        }

        try {
            final TileEntity tile = player.world.getTileEntity(pos);

            // Could be normal, as data changes in main thread... especially given latency
            if(tile == null || !codex.isValid(tile)) {
                PacketTileEvents.onInvalidTile(codex, world, pos);
                return;
            }

            final TARGET target = (TARGET) codex.getConverter().apply(tile);
            if(target != null) {
                setters.forEach(c -> c.accept(target));
            }
            if(codex.onFinished() != null) {
                codex.onFinished().accept(tile, target, player);
            }
        }
        catch (Exception e) {
           PacketTileEvents.onHandlingError(codex, world, pos, e);
        }
    }
}
