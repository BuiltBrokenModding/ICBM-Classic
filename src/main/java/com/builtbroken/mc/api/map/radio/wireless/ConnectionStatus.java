package com.builtbroken.mc.api.map.radio.wireless;

/**
 * Generic connection responses from tiles connected to a wireless network. Mainly
 * used by other tiles that are passing information back to the network. For
 * example ICBM's Silo control station where data about each silo needs to be reported.
 *
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 4/21/2016.
 */
public enum ConnectionStatus
{
    /** Tile has no power, but exists */
    OFFLINE,
    /** Tile is talking to the network */
    ONLINE,
    /** Tile is missing, eg world most likely unloaded it */
    NO_CONNECTION
}
