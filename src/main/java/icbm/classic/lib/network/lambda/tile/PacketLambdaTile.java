package icbm.classic.lib.network.lambda.tile;

import icbm.classic.lib.network.PacketEvents;
import icbm.classic.lib.network.lambda.PacketCodex;
import icbm.classic.lib.network.lambda.PacketCodexReg;
import icbm.classic.lib.tracker.EventTrackerHelpers;
import io.netty.buffer.ByteBuf;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DeferredWorkQueue;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Data
@NoArgsConstructor
public class PacketLambdaTile<TARGET> {

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

    public PacketLambdaTile(PacketCodexTile codex, World world, int x, int y, int z, TARGET target) {
        this.codex = codex;

        setDimensionId(world.getDimension().getType().getId());
        setPos(new BlockPos(x, y, z));

        writers = codex.encodeAsWriters(target);
    }



    public void encode(PacketBuffer buffer) {
        // Write general data
        buffer.writeInt(codex.getId());
        buffer.writeInt(dimensionId);
        buffer.writeInt(pos.getX());
        buffer.writeInt(pos.getY());
        buffer.writeInt(pos.getZ());

        // Write data from builder
        writers.forEach(c -> c.accept(buffer));
    }

    public static PacketLambdaTile decode(PacketBuffer buffer) {
        final PacketLambdaTile packet = new PacketLambdaTile();

        // Read general data
        packet.codex = PacketCodexReg.get(buffer.readInt());
        packet.dimensionId = buffer.readInt();
        packet.pos = new BlockPos(buffer.readInt(), buffer.readInt(), buffer.readInt());

        // Read data for builder
        packet.setters = packet.codex.decodeAsSetters(buffer);

        return packet;

    }

    public static void handle(PacketLambdaTile packet, Supplier<NetworkEvent.Context> contextSupplier) {
       switch (contextSupplier.get().getDirection()) {
           case PLAY_TO_CLIENT:
               contextSupplier.get().enqueueWork(() -> packet.handleClientSide(Minecraft.getInstance(), Objects.requireNonNull(contextSupplier.get().getSender())));
               break;
           case PLAY_TO_SERVER:
               contextSupplier.get().enqueueWork(() -> packet.handleServerSide(Objects.requireNonNull(contextSupplier.get().getSender())));
               break;
           default:
               throw new IllegalStateException("Unexpected value: " + contextSupplier.get().getDirection());
       }
    }

    @OnlyIn(Dist.CLIENT)
    public void handleClientSide(final Minecraft minecraft, final PlayerEntity player)
    {
        final int playerDim = player.world.getDimension().getType().getId();

        // Normal, player may have changed dim between network calls
        if (playerDim != getDimensionId()) {
            PacketEvents.onWrongWorld(codex, EventTrackerHelpers.SIDE_CLIENT, getDimensionId(), playerDim);
            return;
        }

        final World world = player.world;

        DeferredWorkQueue.runLater(() -> loadDataIntoTile(world, player));
    }

    public void handleServerSide(PlayerEntity player)
    {
        final int playerDim = player.world.getDimension().getType().getId();

        // Normal, player may have changed dim between network calls
        if (playerDim != getDimensionId()) {
            PacketEvents.onWrongWorld(codex, EventTrackerHelpers.SIDE_SERVER, getDimensionId(), playerDim);
            return;
        }

        // Issue, should never happen
        if(!(player.world instanceof ServerWorld)) {
            PacketEvents.onNotServerWorld(codex, getDimensionId());
            return;
        }

        final ServerWorld world = (ServerWorld) player.world;
        DeferredWorkQueue.runLater(() -> loadDataIntoTile(world, player));
    }

    private void loadDataIntoTile(World world, PlayerEntity player) {

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
