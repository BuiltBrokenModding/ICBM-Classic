package com.builtbroken.mc.api.map.radio;

/**
 * Used by objects to report status of functionality of radio antenna's.
 * <p>
 * Can be used by anything but is mainly designed for Radio Towers.
 *
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 4/21/2016.
 */
public enum RadioTowerStatus
{
    /** Radio tower is offline */
    OFFLINE,
    /** Tower can only send messages */
    SEND_ONLY,
    /** Tower can only receive messages */
    RECEIVE_ONLY,
    /** Tower is fully functional */
    ONLINE;

    /**
     * Used for methods that toggle between status
     * See ICBM's TileAntenna
     *
     * @return next value
     */
    public RadioTowerStatus next()
    {
        if (this == ONLINE)
        {
            return OFFLINE;
        }
        return values()[ordinal() + 1];
    }
}
