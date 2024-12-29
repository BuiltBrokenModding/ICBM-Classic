package icbm.classic.lib.network.netty;

import com.builtbroken.jlib.data.vector.IPos3D;
import icbm.classic.ICBMClassic;
import icbm.classic.ICBMConstants;
import icbm.classic.api.data.IWorldPosition;
import icbm.classic.lib.network.IPacket;
import icbm.classic.lib.network.lambda.entity.PacketLambdaEntity;
import icbm.classic.lib.network.lambda.tile.PacketLambdaTile;
import icbm.classic.lib.network.packet.PacketEntityPos;
import icbm.classic.lib.network.packet.PacketPlayerItem;
import icbm.classic.lib.network.packet.PacketSpawnAirParticle;
import icbm.classic.lib.network.packet.PacketSpawnBlockExplosion;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.simple.SimpleChannel;

import java.util.EnumMap;

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
        .networkProtocolVersion(() -> PROTOCOL_VERSION)
        .simpleChannel();

    private int nextID = 0;

    public PacketManager(String channel)
    {
        this.channel = channel;
    }

    public void init()
    {
        HANDLER.registerMessage(nextID++, PacketLambdaTile.class, PacketLambdaTile::encode, PacketLambdaTile::decode, PacketLambdaTile::handle);
        HANDLER.registerMessage(nextID++, PacketLambdaEntity.class, PacketLambdaEntity::encode, PacketLambdaEntity::decode, PacketLambdaEntity::handle);

        addPacket(PacketPlayerItem.class);
        addPacket(PacketSpawnAirParticle.class);
        addPacket(PacketSpawnBlockExplosion.class);
        addPacket(PacketEntityPos.class);
    }

    /**
     * @param packet the packet to send to the player
     * @param player the player MP object
     */
    public <MSG> void sendToPlayer(MSG packet, ServerPlayerEntity player)
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
    public <MSG> void sendToAllInDimension(MSG packet, DimensionType dimId)
    {
        HANDLER.send(PacketDistributor.DIMENSION.with(() -> dimId), packet);
    }

    public <MSG> void sendToAllInDimension(MSG packet, World world)
    {
        sendToAllInDimension(packet, world.dimension.getType());
    }

    /**
     * sends to all clients connected to the server
     *
     * @param packet the packet to send.
     */
    public <MSG> void sendToAll(MSG packet)
    {
        HANDLER.send(PacketDistributor.ALL.noArg(), packet);
    }

    public <MSG> void sendToAllAround(MSG message, PacketDistributor.TargetPoint point)
    {
        HANDLER.send(PacketDistributor.NEAR.with(() -> point), message);
    }

    public <MSG> void sendToAllAround(MSG message, IWorldPosition point, double range)
    {
        sendToAllAround(message, point.world(), point.x(), point.y(), point.z(), range);
    }

    public <MSG> void sendToAllAround(MSG message, World world, IPos3D point, double range)
    {
        sendToAllAround(message, world, point.x(), point.y(), point.z(), range);
    }

    public <MSG> void sendToAllAround(MSG message, TileEntity tile)
    {
        sendToAllAround(message, tile, 64);
    }

    public <MSG> void sendToAllAround(MSG message, TileEntity tile, double range)
    {
        sendToAllAround(message, tile.getWorld(), tile.getPos().getX(), tile.getPos().getY(), tile.getPos().getZ(), range);
    }

    public <MSG> void sendToAllAround(MSG message, World world, double x, double y, double z, double range)
    {
        if (world != null)
            sendToAllAround(message, new PacketDistributor.TargetPoint(null, x, y, z, range, world.dimension.getType()));
    }

    public <MSG> void sendToServer(MSG packet)
    {
        HANDLER.sendToServer(packet);
    }
}


