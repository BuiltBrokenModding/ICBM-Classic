package com.builtbroken.mc.api.map.radio.wireless;

import com.builtbroken.mc.api.IWorldPosition;
import com.builtbroken.mc.api.map.radio.IRadioWaveReceiver;
import com.builtbroken.mc.api.map.radio.IRadioWaveSender;
import net.minecraft.util.EnumFacing;

import java.util.List;

/**
 * An object that is attached to the wireless network to provide or receive data.
 * <p>
 * The object normally is not {@link IRadioWaveReceiver} or {@link IRadioWaveSender} but is attached to either to supply information. Think
 * of this like a computer attached to a wireless card. The card itself is the sender/receiver and the computer is the wireless object.
 * <p>
 * If a device is a sender or receiver then it shouldn't implement this interface. As this is only used to pass information threw
 * senders and receivers without calling {@link IRadioWaveSender#sendRadioMessage(float, String, Object...)}
 * to save on performance.
 *
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 4/21/2016.
 */
public interface IWirelessNetworkObject extends IWorldPosition
{
    /**
     * Called to add a network to this wireless device.
     * Range check is already completed before calling
     * this method.
     *
     * @param network - network to add
     * @return true if the network was added.
     */
    boolean addWirelessNetwork(IWirelessNetwork network);

    /**
     * Called to remove a network from this tile. Normally
     * called when the network is terminated, range is lost,
     * or world unloads.
     *
     * @param network - network being removed
     * @return true if the network was removed
     */
    boolean removeWirelessNetwork(IWirelessNetwork network, ConnectionRemovedReason reason);

    /**
     * Called to check if this object can connect to the network.
     * <p>
     * If this object is not a sender/receiver then this check
     * should be based on none radio based values. If
     * this is a sender/receiver then check for range,
     * and frequency to reduce conflict of problems.
     *
     * @param network - network checking for connection
     * @return true if the network can connect
     */
    boolean canConnectToNetwork(IWirelessNetwork network);

    /**
     * Gets a list of all networks attached to this device
     *
     * @return
     */
    List<IWirelessNetwork> getAttachedNetworks();

    /**
     * Checks if can accept connection to antenna on side
     *
     * @param side - side of the tile, going towards the tile
     * @return true if it can accept connection
     */
    boolean canAcceptAntennaConnection(EnumFacing side);
}
