package com.builtbroken.mc.api.map.radio.wireless;

import com.builtbroken.mc.api.map.radio.IRadioWaveReceiver;

import java.util.List;

/**
 * Applied to {@link IRadioWaveReceiver} that allow connections to networks
 *
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 4/21/2016.
 */
public interface IWirelessConnector extends IWirelessNetworkObject
{
    /**
     * Gets all objects connected to the network threw this tile
     *
     * @return list or empty list
     */
    List<IWirelessNetworkObject> getWirelessNetworkObjects();
}
