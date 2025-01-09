package icbm.classic.lib.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.PlayerEntity;

/**
 * Applied to an object that received packet server or client side with an ID
 * Created by Darkguardsman on 8/5/2014.
 */
@Deprecated
public interface IPacketIDReceiver
{
    /**
     * Called to read the packet when received
     *
     * @param buf    - packet data
     * @param id     - packet ID
     * @param player - player who received or sent the packet
     * @param type   - packet object
     * @return true if the data was read
     */
    boolean read(ByteBuf buf, int id, PlayerEntity player, IPacket type);
}
