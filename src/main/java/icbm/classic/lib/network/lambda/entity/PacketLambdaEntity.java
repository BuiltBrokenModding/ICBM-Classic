package icbm.classic.lib.network.lambda.entity;

import icbm.classic.ICBMClassic;
import icbm.classic.lib.network.IPacket;
import icbm.classic.lib.network.lambda.PacketCodex;
import icbm.classic.lib.network.lambda.PacketCodexReg;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

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
    private int dimensionId;
    private int entityId;
    
    private List<Consumer<ByteBuf>> writers;
    private List<Consumer<TARGET>> setters;
    public PacketLambdaEntity(PacketCodex<Entity, TARGET> codex, Entity entity, TARGET target) {
        this.codex = codex;
        this.entityId = entity.getEntityId();
        this.dimensionId = entity.world.provider.getDimension();
        this.writers = codex.encodeAsWriters(target);
    }

    @Override
    public void encodeInto(ChannelHandlerContext ctx, ByteBuf buffer) {
        // Write general data
        buffer.writeInt(codex.getId());
        buffer.writeInt(entityId);
        buffer.writeInt(dimensionId);

        // Write data from builder
        writers.forEach(c -> c.accept(buffer));
    }

    @Override
    public void decodeInto(ChannelHandlerContext ctx, ByteBuf buffer) {

        // Read general data
        final int codexId = buffer.readInt();
        codex = PacketCodexReg.get(codexId);
        if(codex == null) {
            ICBMClassic.logger().error(String.format("PacketEntity: Failed to locate codex(%s)", codexId));
            return;
        }

        entityId = buffer.readInt();
        dimensionId = buffer.readInt();

        // Read data for builder
        setters = codex.decodeAsSetters(buffer);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void handleClientSide(Minecraft minecraft, EntityPlayer player)
    {
        final int playerDim = player.world.provider.getDimension();

        // Normal, player may have changed dim between network calls
        if (playerDim != getDimensionId()) {
            final String message = String.format(DEBUG_WRONG_DIM, getDimensionId(), playerDim);
            codex.logDebug(player.world, player.getPosition(), message);
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
            final String message = String.format(DEBUG_WRONG_DIM, getDimensionId(), playerDim);
            codex.logDebug(player.world, player.getPosition(), message);
            return;
        }

        // Issue, should never happen
        if(!(player.world instanceof WorldServer)) {
            final String message = String.format(ERROR_NOT_SERVER, getDimensionId());
            codex.logError(player.world, player.getPosition(), message);
            return;
        }

        final WorldServer world = (WorldServer) player.world;
        world.addScheduledTask(() -> loadDataIntoTile(world, player));
    }

    private void loadDataIntoTile(World world, EntityPlayer player) {
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
