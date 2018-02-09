package com.builtbroken.mc.api.map.radio.wireless;

import com.builtbroken.mc.api.map.radio.IRadioWaveSender;

import java.util.List;

/**
 * Applied to objects that act as a link cache between senders and receivers
 *
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 4/21/2016.
 */
public interface IWirelessNetwork
{
    /**
     * Heart of the wireless network and main source
     * of data being sent threw the network.
     * <p>
     * Each network should only have one sender for data.
     * Additional senders need to be treated as
     * relay stations for passing data around. This
     * way its easy for tiles to collect data from
     * the network.
     *
     * @return primary sender, if null this is an invalid network.
     */
    IWirelessNetworkHub getPrimarySender();

    /**
     * Gets a list of receives who provide wireless
     * connections to the network
     *
     * @return list of receivers, or empty list
     */
    List<IWirelessConnector> getWirelessConnectors();

    /**
     * Gets a list of senders that can act as
     * relay stations for additional data.
     * <p>
     * In order for a station to be a relay it
     * needs to be a receiver and a sender. It
     * also needs to be with in the primary sender's
     * radio range.
     *
     * @return list of relay stations, or empty list.
     */
    List<IRadioWaveSender> getRelayStations();

    /**
     * Gets a list of objects attached to this network.
     *
     * @return list of attached objects, or empty list
     */
    List<IWirelessNetworkObject> getAttachedObjects();

    /**
     * Frequency
     *
     * @return
     */
    float getHz();

    /**
     * Called when a connector is added
     *
     * @param connector
     */
    boolean addConnection(IWirelessConnector connector, IWirelessNetworkObject object);

    /**
     * Called when a connector is added
     *
     * @param connector
     */
    boolean addConnector(IWirelessConnector connector);

    /**
     * Called when a connector is removed
     *
     * @param connector
     */
    boolean removeConnection(IWirelessConnector connector, IWirelessNetworkObject object);

    /**
     * Called when a connector invalidates
     *
     * @param connector - connector
     */
    boolean removeConnector(IWirelessConnector connector);
}
