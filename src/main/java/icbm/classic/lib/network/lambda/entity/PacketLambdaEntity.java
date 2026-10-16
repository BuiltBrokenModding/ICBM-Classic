package icbm.classic.lib.network.lambda.entity;

import icbm.classic.ICBMClassic;
import icbm.classic.lib.network.IPacket;
import icbm.classic.lib.network.lambda.PacketCodex;
import icbm.classic.lib.network.lambda.PacketCodexReg;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

@Data
@NoArgsConstructor
public class PacketLambdaEntity<TARGET> implements IPacket<PacketLambdaEntity<TARGET>> {

    public static final String ERROR_HANDLING = "unexpected error writing to Entity(%s)\nEntity: %s";
    public static final String ERROR_NOT_SERVER = "Received packet server side but world(%s) is not WorldServer";
    public static final String DEBUG_INVALID = "entity(%s) was invalid\nEntity: %s";
    public static final String DEBUG_WRONG_DIM = "Received packet client side for world(%s) but got world(%s)... ignoring.";

    private PacketCodex<Entity, TARGET> codex;
    private ResourceKey<Level> dimensionId;
    private int entityId;

    private List<Consumer<RegistryFriendlyByteBuf>> writers;
    private List<Consumer<TARGET>> setters;
    public PacketLambdaEntity(PacketCodex<Entity, TARGET> codex, Entity entity, TARGET target) {
        this.codex = codex;
        this.entityId = entity.getId();
        this.dimensionId = entity.level().dimension();
        this.writers = codex.encodeAsWriters(target);
    }

    @Override
    public void encodeInto(RegistryFriendlyByteBuf buffer) {
        // Write general data
        buffer.writeInt(codex.getId());
        buffer.writeInt(entityId);
        buffer.writeResourceKey(dimensionId);

        // Write data from builder
        writers.forEach(c -> c.accept(buffer));
    }

    @Override
    public void decodeInto(RegistryFriendlyByteBuf buffer) {

        // Read general data
        final int codexId = buffer.readInt();
        codex = PacketCodexReg.get(codexId);
        if(codex == null) {
            ICBMClassic.logger().error(String.format("PacketEntity: Failed to locate codex(%s)", codexId));
            // Skip remaining data so the network layer doesn't error on unread bytes
            buffer.skipBytes(buffer.readableBytes());
            return;
        }

        entityId = buffer.readInt();
        dimensionId = buffer.readResourceKey(Registries.DIMENSION);

        // Read data for builder
        setters = codex.decodeAsSetters(buffer);
    }

    @Override
    public void handleClientSide(Minecraft minecraft, Player player)
    {
        if(codex == null || setters == null) {
            return;
        }
        final ResourceKey<Level> playerDim = player.level().dimension();

        // Normal, player may have changed dim between network calls
        if (!playerDim.equals(getDimensionId())) {
            final String message = String.format(DEBUG_WRONG_DIM, getDimensionId(), playerDim);
            codex.logDebug(player.level(), player.blockPosition(), message);
            return;
        }

        loadDataIntoTile(player.level(), player);
    }

    @Override
    public void handleServerSide(Player player)
    {
        if(codex == null || setters == null) {
            return;
        }

        // PORT: 1.12 never checked the codex direction, so a client could push server data into entities, for example
        //  rewriting a flying missile's target through its description codex. No entity codex is sent by clients, so
        //  only accept codexes explicitly marked fromClient() (server only, not bidirectional).
        if(!codex.allowServer() || codex.allowClient()) {
            codex.logError(player.level(), player.blockPosition(),
                String.format("player '%s' sent a codex that is not accepted from clients for entity(%s), ignoring", player.getName().getString(), getEntityId()));
            return;
        }
        final ResourceKey<Level> playerDim = player.level().dimension();

        // Normal, player may have changed dim between network calls
        if (!playerDim.equals(getDimensionId())) {
            final String message = String.format(DEBUG_WRONG_DIM, getDimensionId(), playerDim);
            codex.logDebug(player.level(), player.blockPosition(), message);
            return;
        }

        // Issue, should never happen
        if(!(player.level() instanceof ServerLevel)) {
            final String message = String.format(ERROR_NOT_SERVER, getDimensionId());
            codex.logError(player.level(), player.blockPosition(), message);
            return;
        }

        loadDataIntoTile(player.level(), player);
    }

    private void loadDataIntoTile(Level world, Player player) {
        Entity entity = null;
        try {
            entity = world.getEntity(getEntityId());

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
                codex.logDebug(world, Optional.ofNullable(entity).map(Entity::blockPosition).orElse(null), String.format(DEBUG_INVALID, getEntityId(), entity));
            }
        }
        catch (Exception e) {
            codex.logError(world, Optional.ofNullable(entity).map(Entity::blockPosition).orElse(null), String.format(ERROR_HANDLING, getEntityId(), entity), e);
        }
    }
}
