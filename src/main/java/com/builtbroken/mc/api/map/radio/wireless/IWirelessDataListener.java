package com.builtbroken.mc.api.map.radio.wireless;

/**
 * Applied to objects that are waiting for data or data connections to be added to the network
 *
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 4/21/2016.
 */
public interface IWirelessDataListener
{
    /**
     * Called when an object is added to the network
     *
     * @param object - the object
     */
    void onConnectionAdded(IWirelessNetworkObject object);

    /**
     * Called when an object is removed from the network
     *
     * @param object
     */
    void onConnectionRemoved(IWirelessNetworkObject object, ConnectionRemovedReason reason);
}
