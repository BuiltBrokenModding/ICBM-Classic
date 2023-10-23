package icbm.classic.lib.network.lambda.entity;

import icbm.classic.ICBMConstants;
import icbm.classic.lib.network.lambda.PacketCodex;
import icbm.classic.lib.network.lambda.tile.PacketLambdaTile;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.NetworkRegistry;

import java.util.Optional;
import java.util.function.Function;

/**
 * Codex for creating packets specific to TileEntity data
 *
 * @param <R> raw object, usually a TileEntity or Entity that may contain stuff
 * @param <T> target type for read/write
 */
public class PacketCodexEntity<R extends Entity, T> extends PacketCodex<R, T> {

    public PacketCodexEntity(ResourceLocation parent, String name, Function<R, T> converter) {
        this(parent, new ResourceLocation(ICBMConstants.DOMAIN, name), converter);
    }
    public PacketCodexEntity(ResourceLocation parent, ResourceLocation name, Function<R, T> converter) {
        super(parent, name, converter);
    }

    public PacketCodexEntity(ResourceLocation parent, ResourceLocation name) {
        this(parent, name, (tile) -> (T) tile);
    }
    public PacketCodexEntity(ResourceLocation parent, String name) {
        this(parent, new ResourceLocation(ICBMConstants.DOMAIN, name));
    }
    public void sendToAllAround(R tile){
        double range = 200;
        // TODO consider getting player's chunk map instead
        if(tile.world instanceof WorldServer) {
            final WorldServer worldServer = (WorldServer) tile.world;
            range = Optional.ofNullable(worldServer.getMinecraftServer())
                .map(MinecraftServer::getPlayerList)
                .map(PlayerList::getViewDistance)
                .map(d -> d * 16 + 1.0)
                .orElse(range);
        }
        this.sendToAllAround(tile, range);
    }

    public void sendToAllAround(R tile, double range){
        super.sendToAllAround(tile, new NetworkRegistry.TargetPoint(
            tile.world.provider.getDimension(),
            tile.posX,
            tile.posY,
            tile.posZ,
            range
        ));
    }

    @Override
    public boolean isValid(Entity tile) {
        return tile != null && tile.isEntityAlive();
    }

    public PacketLambdaEntity<T> build(R tile) {
        return new PacketLambdaEntity<T>((PacketCodex<Entity, T>) this, tile, getConverter().apply(tile));
    }
}
