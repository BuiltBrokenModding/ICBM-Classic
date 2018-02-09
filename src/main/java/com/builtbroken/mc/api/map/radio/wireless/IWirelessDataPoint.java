package com.builtbroken.mc.api.map.radio.wireless;

/**
 * Applied to network objects that provide data to the network
 *
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 4/21/2016.
 */
public interface IWirelessDataPoint extends IWirelessNetworkObject
{
    /**
     * Check to see if the data can be accessed with the key
     *
     * @param dataName - name of the data type
     * @param passKey  - passKey for the data
     * @return true if the data can be accessed
     */
    boolean hasAccessForData(String dataName, short passKey);
}
