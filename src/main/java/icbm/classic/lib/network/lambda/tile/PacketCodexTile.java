package icbm.classic.lib.network.lambda.tile;

import icbm.classic.ICBMConstants;
import icbm.classic.lib.network.lambda.PacketCodex;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.Optional;
import java.util.function.Function;

/**
 * Codex for creating packets specific to TileEntity data
 *
 * @param <RAW> raw object, usually a TileEntity or Entity that may contain stuff
 * @param <TARGET> target type for read/write
 */
public class PacketCodexTile<RAW extends TileEntity, TARGET> extends PacketCodex<RAW, TARGET> {

    public PacketCodexTile(ResourceLocation parent, String name, Function<RAW, TARGET> converter) {
        this(parent, new ResourceLocation(ICBMConstants.DOMAIN, name), converter);
    }
    public PacketCodexTile(ResourceLocation parent, ResourceLocation name, Function<RAW, TARGET> converter) {
        super(parent, name, converter);
    }

    public PacketCodexTile(ResourceLocation parent, ResourceLocation name) {
        this(parent, name, (tile) -> (TARGET) tile);
    }
    public PacketCodexTile(ResourceLocation parent, String name) {
        this(parent, new ResourceLocation(parent.getNamespace(), name));
    }

    public void sendToAllAround(RAW tile){
        double range = 64;
        // TODO consider getting player's chunk map instead
        if(tile.getWorld() instanceof ServerWorld) {
            final ServerWorld worldServer = (ServerWorld) tile.getWorld();
            range = Optional.ofNullable(worldServer.getServer())
                .map(MinecraftServer::getPlayerList)
                .map(PlayerList::getViewDistance)
                .map(d -> d * 16 + 1.0)
                .orElse(range);
        }
        this.sendToAllAround(tile, range);
    }

    public void sendToAllAround(RAW tile, double range){
        super.sendToAllAround(tile, new PacketDistributor.TargetPoint(
            null,
            tile.getPos().getX(),
            tile.getPos().getY(),
            tile.getPos().getZ(),
            range,
            tile.getWorld().dimension.getType()
        ));
    }

    @Override
    public boolean isValid(TileEntity tile) {
        return tile != null && !tile.isRemoved();
    }

    @Override
    public PacketLambdaTile<TARGET> build(RAW tile) {
        return new PacketLambdaTile<TARGET>(this, tile, getConverter().apply(tile));
    }
}
