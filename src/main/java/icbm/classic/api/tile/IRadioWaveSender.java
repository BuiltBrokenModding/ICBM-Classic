package icbm.classic.api.tile;

import icbm.classic.api.data.IWorldPosition;
import icbm.classic.lib.transform.region.Cube;

/**
 * Applied to objects that send radio waves
 *
 *
 * Created by Dark(DarkGuardsman, Robert) on 4/20/2016.
 */
@Deprecated //Will be completely replaced by new system
public interface IRadioWaveSender extends IWorldPosition
{
    /**
     * Confirmation that the wave was received, only
     * used for code logic.
     * <p>
     * In no way is this a mirror of how radio waves work in real life. In
     * other words just use this to confirm logic.
     * Use {@link IRadioWaveReceiver} to actually
     * handle real confirmation messages.
     *
     * @param receiver - object that recieved the message,
     *                 this will be called several times
     *                 for each tower the recieves the message
     *
     * @param hz       - frequency of the message on
     * @param header   - description of the data
     * @param data     - data that was sent
     */
    default void onMessageReceived(IRadioWaveReceiver receiver, float hz, String header, Object[] data) /// TODO switch header and data to packet object
    {
        //Optional, not really used by most tiles, only exists for special use cases
    }

    /**
     * Either called internally or by an external machine to
     * send a message threw this device.
     * <p>
     * If external, ensure there is a valid connection with
     * the machine.
     *
     * @param hz     - frequency to send the message on
     * @param header - description of the data
     * @param data   - data being sent
     */
    void sendRadioMessage(float hz, String header, Object... data);

    /**
     * Gets the range of the sender as
     * a cube.
     * <p>
     * Range should be based on size
     * of the antenna and power
     * of the sending unit.
     *
     * @return cube
     */
    Cube getRadioSenderRange();
}
