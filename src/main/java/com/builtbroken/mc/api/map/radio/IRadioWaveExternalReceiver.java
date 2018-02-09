package com.builtbroken.mc.api.map.radio;

import com.builtbroken.mc.imp.transform.region.Cube;

/**
 * Applied to tiles that connect to {@link IRadioWaveReceiver} but are not antenna's
 *
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 4/20/2016.
 */
public interface IRadioWaveExternalReceiver
{
    /**
     * Called when a message is received
     *
     * @param hz            - frequency the message is on, ignore if this
     *                      receive is tuned to a set frequency.
     * @param sender        - object that sent the message.
     * @param receiver      - antenna who picked up the message
     * @param messageHeader - quick way to ID the data being passed into the message
     * @param data          - data from the message
     */
    void receiveExternalRadioWave(float hz, IRadioWaveSender sender, IRadioWaveReceiver receiver, String messageHeader, Object[] data);

    /**
     * Called when receiver changes its coverage range
     *
     * @param range - new range, null if receiver died
     */
    void onRangeChange(IRadioWaveReceiver receiver, Cube range);
}
