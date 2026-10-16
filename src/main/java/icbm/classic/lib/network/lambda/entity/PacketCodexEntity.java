package icbm.classic.lib.network.lambda.entity;

import icbm.classic.ICBMConstants;
import icbm.classic.lib.network.TargetPoint;
import icbm.classic.lib.network.lambda.PacketCodex;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.Entity;

import java.util.Optional;
import java.util.function.Function;

/**
 * Codex for creating packets specific to Entity data
 *
 * @param <R> raw object, usually a TileEntity or Entity that may contain stuff
 * @param <T> target type for read/write
 */
public class PacketCodexEntity<R extends Entity, T> extends PacketCodex<R, T> {

    public PacketCodexEntity(ResourceLocation parent, String name, Function<R, T> converter) {
        this(parent, ResourceLocation.fromNamespaceAndPath(ICBMConstants.DOMAIN, name), converter);
    }
    public PacketCodexEntity(ResourceLocation parent, ResourceLocation name, Function<R, T> converter) {
        super(parent, name, converter);
    }

    public PacketCodexEntity(ResourceLocation parent, ResourceLocation name) {
        this(parent, name, (tile) -> (T) tile);
    }
    public PacketCodexEntity(ResourceLocation parent, String name) {
        this(parent, ResourceLocation.fromNamespaceAndPath(ICBMConstants.DOMAIN, name));
    }
    public void sendToAllAround(R tile){
        double range = 200;
        // TODO consider getting player's chunk map instead
        if(tile.level() instanceof ServerLevel) {
            final ServerLevel worldServer = (ServerLevel) tile.level();
            range = Optional.ofNullable(worldServer.getServer())
                .map(MinecraftServer::getPlayerList)
                .map(PlayerList::getViewDistance)
                .map(d -> d * 16 + 1.0)
                .orElse(range);
        }
        this.sendToAllAround(tile, range);
    }

    public void sendToAllAround(R tile, double range){
        super.sendToAllAround(tile, new TargetPoint(
            tile.level().dimension(),
            tile.getX(),
            tile.getY(),
            tile.getZ(),
            range
        ));
    }

    @Override
    public boolean isValid(Entity tile) {
        return tile != null && tile.isAlive();
    }

    public PacketLambdaEntity<T> build(R tile) {
        return new PacketLambdaEntity<T>((PacketCodex<Entity, T>) this, tile, getConverter().apply(tile));
    }
}
