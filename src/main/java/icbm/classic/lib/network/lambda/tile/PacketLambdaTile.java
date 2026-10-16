package icbm.classic.lib.network.lambda.tile;

import icbm.classic.ICBMClassic;
import icbm.classic.lib.network.IPacket;
import icbm.classic.lib.network.PacketEvents;
import icbm.classic.lib.network.lambda.PacketCodex;
import icbm.classic.lib.network.lambda.PacketCodexReg;
import icbm.classic.lib.tracker.EventTrackerHelpers;
import icbm.classic.prefab.gui.ContainerBase;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.List;
import java.util.function.Consumer;

@Data
@NoArgsConstructor
public class PacketLambdaTile<TARGET> implements IPacket<PacketLambdaTile<TARGET>> {

    private PacketCodex codex;
    private ResourceKey<Level> dimensionId;
    private BlockPos pos;
    private List<Consumer<RegistryFriendlyByteBuf>> writers;
    private List<Consumer<TARGET>> setters;

    public PacketLambdaTile(PacketCodexTile codex, BlockEntity tile, TARGET target) {
        this(codex, tile.getLevel(), tile.getBlockPos(), target);
    }

    public PacketLambdaTile(PacketCodexTile codex, Level dimensionId, BlockPos pos, TARGET target) {
      this(codex, dimensionId, pos.getX(), pos.getY(), pos.getZ(), target);
    }

    public PacketLambdaTile(PacketCodexTile codex, Level dimensionId, int x, int y, int z, TARGET target) {
        this.codex = codex;

        setDimensionId(dimensionId.dimension());
        setPos(new BlockPos(x, y, z));

        writers = codex.encodeAsWriters(target);
    }

    @Override
    public void encodeInto(RegistryFriendlyByteBuf buffer) {
        // Write general data
        buffer.writeInt(codex.getId());
        buffer.writeResourceKey(dimensionId);
        buffer.writeBlockPos(pos);

        // Write data from builder
        writers.forEach(c -> c.accept(buffer));
    }

    @Override
    public void decodeInto(RegistryFriendlyByteBuf buffer) {

        // Read general data
        final int codexId = buffer.readInt();
        codex = PacketCodexReg.get(codexId);
        if(codex == null) {
            // PORT: 1.12 caught decode errors in the netty handler, in 1.21 an exception while decoding drops the connection
            ICBMClassic.logger().error(String.format("PacketTile: Failed to locate codex(%s)", codexId));
            // Skip remaining data so the network layer doesn't error on unread bytes
            buffer.skipBytes(buffer.readableBytes());
            return;
        }
        dimensionId = buffer.readResourceKey(Registries.DIMENSION);
        pos = buffer.readBlockPos();

        // Read data for builder
        setters = codex.decodeAsSetters(buffer);
    }

    @Override
    public void handleClientSide(final Minecraft minecraft, final Player player)
    {
        if(codex == null || setters == null) {
            return;
        }
        final ResourceKey<Level> playerDim = player.level().dimension();

        // Normal, player may have changed dim between network calls
        if (!playerDim.equals(getDimensionId())) {
            PacketEvents.onWrongWorld(codex, EventTrackerHelpers.SIDE_CLIENT, getDimensionId(), playerDim);
            return;
        }

        loadDataIntoTile(player.level(), player, false);
    }

    @Override
    public void handleServerSide(Player player)
    {
        if(codex == null || setters == null) {
            return;
        }

        // PORT: 1.12 never checked the codex direction, so clients could push server to client data (energy, missile
        //  stacks) into tiles. Only codexes marked fromClient() are accepted from players.
        if(!codex.allowServer()) {
            ICBMClassic.logger().warn(String.format("PacketTile: player '%s' sent server to client only codex %s for %s, ignoring",
                player.getName().getString(), codex, pos));
            return;
        }

        final ResourceKey<Level> playerDim = player.level().dimension();

        // Normal, player may have changed dim between network calls
        if (!playerDim.equals(getDimensionId())) {
            PacketEvents.onWrongWorld(codex, EventTrackerHelpers.SIDE_SERVER, getDimensionId(), playerDim);
            return;
        }

        // Issue, should never happen
        if(!(player.level() instanceof ServerLevel)) {
            PacketEvents.onNotServerWorld(codex, getDimensionId());
            return;
        }

        loadDataIntoTile(player.level(), player, true);
    }

    /**
     * Checks that the player sending a packet to the server is using the tile's GUI. Every client to server tile
     * codex is sent from a machine GUI, and the open menu also enforces the 8 block use range.
     */
    private static boolean isUsingTile(Player player, BlockEntity tile) {
        return player.containerMenu instanceof ContainerBase<?> menu
            && menu.getHost() == tile
            && menu.stillValid(player);
    }

    private void loadDataIntoTile(Level world, Player player, boolean fromClient) {

        // Area is no longer loaded, this is normal in most cases
        if(!world.isLoaded(pos)) {
            return;
        }

        try {
            final BlockEntity tile = world.getBlockEntity(pos);

            // Could be normal, as data changes in main thread... especially given latency
            if(tile == null || !codex.isValid(tile)) {
                PacketTileEvents.onInvalidTile(codex, world, pos);
                return;
            }

            // PORT: 1.12 accepted packets for any loaded tile in the dimension at any distance, so any player could
            //  fire or reconfigure machines remotely. Require the sender to have this tile's GUI open.
            if(fromClient && !isUsingTile(player, tile)) {
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
