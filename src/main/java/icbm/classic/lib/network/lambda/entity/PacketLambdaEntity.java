package icbm.classic.lib.network.lambda.entity;

import icbm.classic.ICBMClassic;
import icbm.classic.lib.network.lambda.PacketCodex;
import icbm.classic.lib.network.lambda.PacketCodexReg;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Data
@NoArgsConstructor
public class PacketLambdaEntity<TARGET> {

    public static final String ERROR_HANDLING = "unexpected error writing to Entity(%s)\nEntity: %s";
    public static final String ERROR_NOT_SERVER = "Received packet server side but world(%s) is not WorldServer";
    public static final String DEBUG_INVALID = "entity(%s) was invalid\nEntity: %s";
    public static final String DEBUG_WRONG_DIM = "Received packet client side for world(%s) but got world(%s)... ignoring.";

    private PacketCodex<Entity, TARGET> codex;
    private int dimensionId;
    private int entityId;
    
    private List<Consumer<PacketBuffer>> writers;
    private List<Consumer<TARGET>> setters;
    public PacketLambdaEntity(PacketCodex<Entity, TARGET> codex, Entity entity, TARGET target) {
        this.codex = codex;
        this.entityId = entity.getEntityId();
        this.dimensionId = entity.world.getDimension().getType().getId();
        this.writers = codex.encodeAsWriters(target);
    }

    public void encode(PacketBuffer buffer) {
        // Write general data
        buffer.writeInt(codex.getId());
        buffer.writeInt(entityId);
        buffer.writeInt(dimensionId);

        // Write data from builder
        writers.forEach(c -> c.accept(buffer));
    }

    public static PacketLambdaEntity decode(PacketBuffer buffer) {
        final PacketLambdaEntity packet = new PacketLambdaEntity();

        // Read general data
        final int codexId = buffer.readInt();
        packet.codex = PacketCodexReg.get(codexId);
        if(packet.codex == null) {
            ICBMClassic.logger().error(String.format("PacketEntity: Failed to locate codex(%s)", codexId));
            return null;
        }

        packet.entityId = buffer.readInt();
        packet.dimensionId = buffer.readInt();

        // Read data for builder
        packet.setters = packet.codex.decodeAsSetters(buffer);

        return packet;

    }

    public static void handle(PacketLambdaEntity packet, Supplier<NetworkEvent.Context> contextSupplier) {
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
    public void handleClientSide(Minecraft minecraft, PlayerEntity player)
    {
        final int playerDim = player.world.dimension.getType().getId();

        // Normal, player may have changed dim between network calls
        if (playerDim != getDimensionId()) {
            final String message = String.format(DEBUG_WRONG_DIM, getDimensionId(), playerDim);
            codex.logDebug(player.world, player.getPosition(), message);
            return;
        }

        final World world = player.world;

        minecraft.enqueue(() -> loadDataIntoTile(world, player));
    }

    public void handleServerSide(PlayerEntity player)
    {
        final int playerDim = player.world.dimension.getType().getId();

        // Normal, player may have changed dim between network calls
        if (playerDim != getDimensionId()) {
            final String message = String.format(DEBUG_WRONG_DIM, getDimensionId(), playerDim);
            codex.logDebug(player.world, player.getPosition(), message);
            return;
        }

        // Issue, should never happen
        if(!(player.world instanceof ServerWorld)) {
            final String message = String.format(ERROR_NOT_SERVER, getDimensionId());
            codex.logError(player.world, player.getPosition(), message);
            return;
        }

        final ServerWorld world = (ServerWorld) player.world;
        world.getServer().execute(() -> loadDataIntoTile(world, player));
    }

    private void loadDataIntoTile(World world, PlayerEntity player) {
        Entity entity = null;
        try {
            entity = player.world.getEntityByID(getEntityId());

            if(entity != null && codex.isValid(entity)) {
                final TARGET target = codex.getConverter().apply(entity);
                if(target != null) {
                    setters.forEach(c -> c.accept(target)); //TODO detect for issues and log so we know which setter failed
                }
                if(codex.onFinished() != null) {
                    codex.onFinished().accept(entity, target, player);
                }
            }
            // This may be valid, as the tile could have changed or may have become invalid.
            // Average ping is same as tick rate so changes on main-thread are expected
            else if(ICBMClassic.logger().isDebugEnabled()) {
                codex.logDebug(world, Optional.ofNullable(entity).map(Entity::getPosition).orElse(null), String.format(DEBUG_INVALID, getEntityId(), entity));
            }
        }
        catch (Exception e) {
            codex.logError(world, Optional.ofNullable(entity).map(Entity::getPosition).orElse(null), String.format(ERROR_HANDLING, getEntityId(), entity), e);
        }
    }
}
