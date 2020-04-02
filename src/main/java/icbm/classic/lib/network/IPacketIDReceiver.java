package icbm.classic.lib.network;

import icbm.classic.api.data.IWorldPosition;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;

/**
 * Applied to an object that received packet server or client side with an ID
 * Created by Darkguardsman on 8/5/2014.
 */
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
    boolean read(ByteBuf buf, int id, EntityPlayer player, IPacket type);

    /**
     * Called to check if the packet should be read at all.
     * <p>
     * Use this to validate packet data to prevent users with hacked clients from corrupting data.
     *
     * @param player          - player who sent the packet
     * @param receiveLocation - location the packet should be read at, will return player location for
     *                        world packets without postion data, and null if non-world packets
     * @return true if the packet should be read
     */
    default boolean shouldReadPacket(EntityPlayer player, IWorldPosition receiveLocation, IPacket packet)
    {
        return true;
    }
}
