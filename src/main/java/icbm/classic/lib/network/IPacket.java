package icbm.classic.lib.network;

import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Player;

import java.util.function.Consumer;

/**
 * Base object for all custom packets using in VoltzEngine.
 * </p>
 * Ensure that there is always a default constructor so the packet can be created on receive.
 * <p>
 * All packets ride a single NeoForge payload, see {@link icbm.classic.lib.network.netty.PacketManager}.
 * Handlers are called on the main thread.
 *
 * @author tgame14, DarkCow
 * @since 26/05/14
 */
public interface IPacket<P extends IPacket>
{

    /**
     * Encode the packet data into the buffer. Complex data sets may need specific data handlers
     *
     * @param buffer the buffer to encode into
     */
    void encodeInto(RegistryFriendlyByteBuf buffer);

    /**
     * Decode the packet data from the buffer. Must consume all bytes written by {@link #encodeInto(RegistryFriendlyByteBuf)}.
     *
     * @param buffer the buffer to decode from
     */
    void decodeInto(RegistryFriendlyByteBuf buffer);

    @Deprecated
    default P addData(Object... objects)
    {
        return (P) this;
    }

    @Deprecated
    default P addData(Consumer<RegistryFriendlyByteBuf> writer) {
        return (P) this;
    }

    /**
     * Handle a packet on the client side. Note this occurs after decoding has completed. Only ever called
     * on the physical client.
     *
     * @param minecraft client instance
     * @param player the client player
     */
    default void handleClientSide(final Minecraft minecraft, final Player player)
    {
        throw new UnsupportedOperationException("Unsupported operation for Packet: " + getClass().getSimpleName());
    }

    /**
     * Handle a packet on the server side. Note this occurs after decoding has completed.
     *
     * @param player the player reference
     */
    default void handleServerSide(Player player)
    {
        throw new UnsupportedOperationException("Unsupported operation for Packet: " + getClass().getSimpleName());
    }
}
