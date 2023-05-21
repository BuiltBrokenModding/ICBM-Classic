package icbm.classic.lib.network.lambda.tile;

import icbm.classic.ICBMConstants;
import icbm.classic.lib.network.lambda.PacketCodex;
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
public class PacketCodexTile<R extends TileEntity, T> extends PacketCodex<R, T> {

    public PacketCodexTile(ResourceLocation parent, String name, Function<R, T> converter) {
        this(parent, new ResourceLocation(ICBMConstants.DOMAIN, name), converter);
    }
    public PacketCodexTile(ResourceLocation parent, ResourceLocation name, Function<R, T> converter) {
        super(parent, name, converter);
    }

    public PacketCodexTile(ResourceLocation parent, ResourceLocation name) {
        this(parent, name, (tile) -> (T) tile);
    }
    public PacketCodexTile(ResourceLocation parent, String name) {
        this(parent, new ResourceLocation(ICBMConstants.DOMAIN, name));
    }

    public void sendToAllAround(R tile){
        double range = 64;
        // TODO consider getting player's chunk map instead
        if(tile.getWorld() instanceof WorldServer) {
            final WorldServer worldServer = (WorldServer) tile.getWorld();
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
            tile.getWorld().provider.getDimension(),
            tile.getPos().getX(),
            tile.getPos().getY(),
            tile.getPos().getZ(),
            range
        ));
    }

    @Override
    public boolean isValid(TileEntity tile) {
        return tile != null && !tile.isInvalid();
    }

    public PacketLambdaTile<T> build(R tile) {
        return new PacketLambdaTile<T>(this, tile, getConverter().apply(tile));
    }
}
