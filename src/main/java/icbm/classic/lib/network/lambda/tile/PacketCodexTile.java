package icbm.classic.lib.network.lambda.tile;

import icbm.classic.ICBMConstants;
import icbm.classic.lib.network.TargetPoint;
import icbm.classic.lib.network.lambda.PacketCodex;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.Optional;
import java.util.function.Function;

/**
 * Codex for creating packets specific to TileEntity data
 *
 * @param <R> raw object, usually a TileEntity or Entity that may contain stuff
 * @param <T> target type for read/write
 */
public class PacketCodexTile<R extends BlockEntity, T> extends PacketCodex<R, T> {

    public PacketCodexTile(ResourceLocation parent, String name, Function<R, T> converter) {
        this(parent, ResourceLocation.fromNamespaceAndPath(ICBMConstants.DOMAIN, name), converter);
    }
    public PacketCodexTile(ResourceLocation parent, ResourceLocation name, Function<R, T> converter) {
        super(parent, name, converter);
    }

    public PacketCodexTile(ResourceLocation parent, ResourceLocation name) {
        this(parent, name, (tile) -> (T) tile);
    }
    public PacketCodexTile(ResourceLocation parent, String name) {
        this(parent, ResourceLocation.fromNamespaceAndPath(parent.getNamespace(), name));
    }

    public void sendToAllAround(R tile){
        double range = 64;
        // TODO consider getting player's chunk map instead
        if(tile.getLevel() instanceof ServerLevel) {
            final ServerLevel worldServer = (ServerLevel) tile.getLevel();
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
            tile.getLevel().dimension(),
            tile.getBlockPos().getX(),
            tile.getBlockPos().getY(),
            tile.getBlockPos().getZ(),
            range
        ));
    }

    @Override
    public boolean isValid(BlockEntity tile) {
        return tile != null && !tile.isRemoved();
    }

    public PacketLambdaTile<T> build(R tile) {
        return new PacketLambdaTile<T>(this, tile, getConverter().apply(tile));
    }
}
