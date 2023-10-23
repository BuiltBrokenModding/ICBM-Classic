package icbm.classic.api.radio;

/**
 * Applied to capabilities to allow receiving radio messages
 */
public interface IRadioReceiver extends IRadio {

    /**
     * Called when a message is received from a sender
     *
     * @param sender who pushed the message
     * @param packet containing the message channel and data
     */
    void onMessage(IRadioSender sender, IRadioMessage packet);
}
