package com.builtbroken.mc.api.map.radio;

import com.builtbroken.mc.api.IWorldPosition;
import com.builtbroken.mc.imp.transform.region.Cube;

/**
 * Applied to objects that act like radio receivers
 *
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 4/20/2016.
 */
public interface IRadioWaveReceiver extends IWorldPosition
{
    /**
     * Called when a message is received
     *
     * @param hz            - frequency the message is on, ignore if this
     *                      receive is tuned to a set frequency.
     * @param sender        - object that sent the message.
     * @param messageHeader - quick way to ID the data being passed into the message
     * @param data          - data from the message
     */
    void receiveRadioWave(float hz, IRadioWaveSender sender, String messageHeader, Object[] data);

    /**
     * Gets the range of the receive as
     * a cube. The range should be equal
     * to the size of the antenna. As
     * a receiver can not pick up a signal
     * outside of its body area.
     *
     *
     * Keep in mind a receiver is
     * a passive object and has a constant
     * range. Use the distance
     * from the sender to gauge strength
     * of signal. Then boost the strength
     * using logic rather than increasing
     * range using power boosting.
     *
     * @return cube
     */
    Cube getRadioReceiverRange();
}
