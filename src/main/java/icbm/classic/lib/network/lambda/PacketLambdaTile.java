package icbm.classic.lib.network.lambda;

import icbm.classic.ICBMClassic;
import icbm.classic.lib.network.IPacket;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
public class PacketLambdaTile<TARGET> implements IPacket<PacketLambdaTile<TARGET>> {

    public static final String ERROR_HANDLING = "Failed to handle packet(%s) for W:[%s] %sx %sy %sz";
    public static final String DEBUG_INVALID_TILE = "Tile was invalid packet(%s) for W:[%s] %sx %sy %sz";

    private int id;
    private int dimensionId;
    private int x;
    private int y;
    private int z;
    private List<Consumer<ByteBuf>> writers;
    private List<Consumer<TARGET>> setters;

    public PacketLambdaTile(int id, TileEntity tile, TARGET target) {
        this(id, tile.getWorld(), tile.getPos(), target);
    }

    public PacketLambdaTile(int id, World dimensionId, BlockPos pos, TARGET target) {
      this(id, dimensionId, pos.getX(), pos.getY(), pos.getZ(), target);
    }

    public PacketLambdaTile(int id, World dimensionId, int x, int y, int z, TARGET target) {
        setId(id);

        // Error if isn't tile
        assert target instanceof TileEntity;

        // Assuming tileEntity set init data
        setDimensionId(dimensionId.provider.getDimension());
        setX(x);
        setY(y);
        setZ(z);

        pullData(target);
    }

    private void pullData(TARGET target) {
        final PacketCodex<TileEntity, TARGET> builder = getBuilder();
        writers = builder.getEntries().stream()
            .map(entry -> {
                final Object o = entry.getGetter().apply(target);
                final BiConsumer<ByteBuf, Object> encoder = (BiConsumer<ByteBuf, Object>) entry.getEncoder();
                return (Consumer<ByteBuf>) (byteBuf) -> encoder.accept(byteBuf, o);
            })
            .collect(Collectors.toList());
    }
    @Override
    public void encodeInto(ChannelHandlerContext ctx, ByteBuf buffer) {
        // Write general data
        buffer.writeInt(id);
        buffer.writeInt(dimensionId);
        buffer.writeInt(x);
        buffer.writeInt(y);
        buffer.writeInt(z);

        // Write data from builder
        writers.forEach(c -> c.accept(buffer));
    }

    @Override
    public void decodeInto(ChannelHandlerContext ctx, ByteBuf buffer) {

        // Read general data
        id = buffer.readInt();
        dimensionId = buffer.readInt();
        x = buffer.readInt();
        y = buffer.readInt();
        z = buffer.readInt();

        // Read data for builder
        final PacketCodex<TileEntity, TARGET> builder = getBuilder();
        setters = builder.getEntries().stream()
            .map(entry -> {
                final Function<ByteBuf, Object> decoder = (Function<ByteBuf, Object>) entry.getDecoder();
                final Object data = decoder.apply(buffer);
                final BiConsumer<TARGET, Object> setter = (BiConsumer<TARGET, Object>) entry.getSetter();
                return (Consumer<TARGET>) (target) -> setter.accept(target, data);
            })
            .collect(Collectors.toList());

    }

    public PacketCodex<TileEntity, TARGET> getBuilder() {
        final PacketCodex<TileEntity, TARGET> builder = (PacketCodex<TileEntity, TARGET>) PacketCodexReg.get(id);
        if(builder == null) {
            throw new RuntimeException("Failed to get packet builder for id " + id);
        }
        return builder;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void handleClientSide(EntityPlayer player)
    {
        final int playerDim = player.world.provider.getDimension();

        // Normal, player may have changed dim between network calls
        if (playerDim != getDimensionId()) {
            ICBMClassic.logger().debug(String.format("Received packet client side for world(%s) but got world(%s)... ignoring.", getDimensionId(), playerDim));
            return;
        }

        final World world = player.world;
        final BlockPos pos = new BlockPos(x, y, z);

        Minecraft.getMinecraft().addScheduledTask(() -> loadDataIntoTile(world, pos, player));
    }

    @Override
    public void handleServerSide(EntityPlayer player)
    {
        final int playerDim = player.world.provider.getDimension();

        // Normal, player may have changed dim between network calls
        if (playerDim != getDimensionId()) {
            ICBMClassic.logger().debug(String.format("Received packet server side for world(%s) but got world(%s)... ignoring.", getDimensionId(), playerDim));
            return;
        }

        // Issue, should never happen
        if(!(player.world instanceof WorldServer)) {
            ICBMClassic.logger().error(String.format("Received packet server side but world(%s) is not WorldServer", getDimensionId()));
            return;
        }

        final WorldServer world = (WorldServer) player.world;
        final BlockPos pos = new BlockPos(x, y, z);
        world.addScheduledTask(() -> loadDataIntoTile(world, pos, player));
    }

    private void loadDataIntoTile(World world, BlockPos pos, EntityPlayer player) {

        // Area is no longer loaded, this is normal in most cases
        if(!world.isBlockLoaded(pos)) {
            return;
        }

        PacketCodex<TileEntity, TARGET> builder = null;
        try {
            builder = (PacketCodex<TileEntity, TARGET>) PacketCodexReg.get(id);

            final TileEntity tile = player.world.getTileEntity(pos);
            if(tile != null && builder.isValid(tile)) {
                final TARGET target = builder.getConverter().apply(tile);
                if(target != null) {
                    setters.forEach(c -> c.accept(target));
                }
                if(builder.onFinished() != null) {
                    builder.onFinished().accept(tile, target, player);
                }
            }
            // This may be valid, as the tile could have changed or may have become invalid.
            // Average ping is same as tick rate so changes on main-thread are expected
            else if(ICBMClassic.logger().isDebugEnabled()) {
                ICBMClassic.logger().debug(buildPacketLog(builder, world, pos, DEBUG_INVALID_TILE));
            }
        }
        catch (Exception e) {
            ICBMClassic.logger().error(buildPacketLog(builder, world, pos, ERROR_HANDLING), e); //TODO add more details
        }
    }

    private String buildPacketLog(PacketCodex builder, World world, BlockPos pos, String template) {
        final String packetName = Optional.ofNullable(builder).map(Object::toString).orElse(null);
        final String worldName = Optional.of(world.getWorldInfo()).map(WorldInfo::getWorldName).orElse("--");
        return String.format(template, packetName, worldName, pos.getX(), pos.getY(), pos.getZ());
    }
}
