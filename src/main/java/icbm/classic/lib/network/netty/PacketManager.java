package icbm.classic.lib.network.netty;

import icbm.classic.ICBMConstants;
import icbm.classic.lib.network.lambda.entity.PacketLambdaEntity;
import icbm.classic.lib.network.lambda.item.PacketLambdaPlayerItem;
import icbm.classic.lib.network.lambda.tile.PacketLambdaTile;
import icbm.classic.lib.network.packet.PacketLaserDetonator;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.simple.SimpleChannel;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @author tgame14
 * @since 26/05/14
 */
public class PacketManager
{
    public final String channel;

    private static final String PROTOCOL_VERSION = Integer.toString(1);
    private static final SimpleChannel HANDLER = NetworkRegistry.ChannelBuilder
        .named(new ResourceLocation(ICBMConstants.DOMAIN, "main_channel"))
        .clientAcceptedVersions(PROTOCOL_VERSION::equals)
        .serverAcceptedVersions(PROTOCOL_VERSION::equals)
        .networkProtocolVersion(() -> PROTOCOL_VERSION) //TODO maybe use mod version?
        .simpleChannel();

    private static int nextID = 0;

    public PacketManager(String channel)
    {
        this.channel = channel;
    }

    public static void register()
    {
        //TODO break out each codex into its own registry entry so we go strait from input -> target without decoding into a middle object
        register(PacketLambdaTile.class, PacketLambdaTile::encode, PacketLambdaTile::decode, PacketLambdaTile::handle);
        register(PacketLambdaEntity.class, PacketLambdaEntity::encode, PacketLambdaEntity::decode, PacketLambdaEntity::handle);
        register(PacketLambdaPlayerItem.class, PacketLambdaPlayerItem::encode, PacketLambdaPlayerItem::decode, PacketLambdaPlayerItem::handle);
        register(PacketLaserDetonator.class, PacketLaserDetonator::encode, PacketLaserDetonator::decode, PacketLaserDetonator::handle);

        //addPacket(PacketSpawnAirParticle.class);
        //addPacket(PacketSpawnBlockExplosion.class);
        //addPacket(PacketEntityPos.class);
    }

    private static <MSG> void register(Class<MSG> type, BiConsumer<MSG, PacketBuffer> encoder, Function<PacketBuffer, MSG> decoder, BiConsumer<MSG, Supplier<NetworkEvent.Context>> handler) {
        HANDLER.registerMessage(nextID++, type, encoder, decoder, handler);
    }

    /**
     * @param packet the packet to send to the player
     * @param player the player MP object
     */
    public static <MSG> void sendToPlayer(MSG packet, ServerPlayerEntity player)
    {
        if (!(player instanceof FakePlayer))
        {
            HANDLER.sendTo(packet, player.connection.netManager, NetworkDirection.PLAY_TO_CLIENT);
        }
    }

    /**
     * @param packet the packet to send to the players in the dimension
     * @param dimId  the dimension ID to send to.
     */
    public static <MSG> void sendToAllInDimension(MSG packet, DimensionType dimId)
    {
        HANDLER.send(PacketDistributor.DIMENSION.with(() -> dimId), packet);
    }

    public static <MSG> void sendToAllInDimension(MSG packet, World world)
    {
        sendToAllInDimension(packet, world.dimension.getType());
    }

    /**
     * sends to all clients connected to the server
     *
     * @param packet the packet to send.
     */
    public static <MSG> void sendToAll(MSG packet)
    {
        HANDLER.send(PacketDistributor.ALL.noArg(), packet);
    }

    public static <MSG> void sendToAllAround(MSG message, PacketDistributor.TargetPoint point)
    {
        HANDLER.send(PacketDistributor.NEAR.with(() -> point), message);
    }

    public static <MSG> void sendToAllAround(MSG message, TileEntity tile)
    {
        sendToAllAround(message, tile, 64);
    }

    public static <MSG> void sendToAllAround(MSG message, TileEntity tile, double range)
    {
        sendToAllAround(message, tile.getWorld(), tile.getPos().getX(), tile.getPos().getY(), tile.getPos().getZ(), range);
    }

    public static <MSG> void sendToAllAround(MSG message, World world, double x, double y, double z, double range)
    {
        if (world != null)
            sendToAllAround(message, new PacketDistributor.TargetPoint(null, x, y, z, range, world.dimension.getType()));
    }

    public static <MSG> void sendToServer(MSG packet)
    {
        HANDLER.sendToServer(packet);
    }
}


