package icbm.classic.lib.network.lambda;

import icbm.classic.ICBMConstants;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

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


    @Override
    public boolean isValid(TileEntity tile) {
        return tile != null && !tile.isInvalid();
    }

    public PacketLambdaTile<T> build(R tile) {
        return new PacketLambdaTile<T>(getId(), tile, getConverter().apply(tile));
    }
}
