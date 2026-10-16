package icbm.classic.lib.network.netty;

import icbm.classic.ICBMConstants;
import icbm.classic.lib.network.IPacket;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Single NeoForge payload carrying every ICBM {@link IPacket}. The packet class is identified by a
 * discriminator assigned in registration order by {@link PacketManager}.
 *
 * @param packet  wrapped packet
 * @param decoded true when the packet was created by {@link #STREAM_CODEC} on receive. False for the sender's own
 *                instance, which is handed over as is on memory connections (singleplayer, LAN host) and still needs
 *                to run through the codec before it can be handled, see {@link PacketManager}.
 */
public record ICBMPayload(IPacket<?> packet, boolean decoded) implements CustomPacketPayload
{
    public static final Type<ICBMPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(ICBMConstants.DOMAIN, "packet"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ICBMPayload> STREAM_CODEC = StreamCodec.of(
        (buf, payload) -> {
            buf.writeVarInt(PacketManager.getDiscriminator(payload.packet()));
            payload.packet().encodeInto(buf);
        },
        (buf) -> {
            final IPacket<?> packet = PacketManager.createPacket(buf.readVarInt());
            packet.decodeInto(buf);
            return new ICBMPayload(packet, true);
        }
    );

    /**
     * @param packet to send, used on the sending side
     */
    public ICBMPayload(IPacket<?> packet)
    {
        this(packet, false);
    }

    @Override
    public Type<? extends CustomPacketPayload> type()
    {
        return TYPE;
    }
}
