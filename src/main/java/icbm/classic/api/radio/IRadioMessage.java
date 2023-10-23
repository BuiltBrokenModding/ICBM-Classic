package icbm.classic.api.radio;

/**
 * Radio message to pass to radio based tiles
 */
public interface IRadioMessage {

    /**
     * Channel for the message
     *
     * @return id
     */
    String getChannel();
}
