package icbm.classic.api.radio;

public interface IRadioSender extends IRadio {

    /**
     * Callback for when a receiver get the message. Radio systems is treated
     * as a two-way communication protocol much like TCP networks.
     * <p>
     * Each time a message is received it will give feedback to display to the user. This
     * allows the sending device to know something has happened. More specifically gives
     * feedback to the user so they understand reactions have happened.
     * <p>
     * Not all devices will provide callback information. Some may be set into silent mode
     * to avoid broadcasting information. As it can be assumed radio traffic can be tracked
     * in both directions.
     */
    void onMessageCallback(IRadioReceiver receiver, IRadioMessage response);
}
