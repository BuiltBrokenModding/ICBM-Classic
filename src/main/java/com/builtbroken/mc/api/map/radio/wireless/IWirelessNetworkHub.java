package com.builtbroken.mc.api.map.radio.wireless;

import com.builtbroken.mc.api.IWorldPosition;
import com.builtbroken.mc.imp.transform.region.Cube;

/**
 * Applied to tiles that act as a wifi-style hub for a wireless network. In other words this is the same
 * as a wireless router.
 *
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 4/21/2016.
 */
public interface IWirelessNetworkHub extends IWorldPosition
{
    /** Area that wirless connections can be made */
    Cube getWirelessCoverageArea();

    /**
     * Gets the network created by this wireless hub
     *
     * @return network, or null if no network is generated
     */
    IWirelessNetwork getWirelessNetwork();

    /**
     * Gets the frequency this hub is sending and receiving messages on
     * for the wireless network
     *
     * @return value greater than zero
     */
    float getBroadCastFrequency();
}
