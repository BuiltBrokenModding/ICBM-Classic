package icbm.classic.api.radio.messages;

import icbm.classic.api.launcher.IActionStatus;
import icbm.classic.api.radio.IRadioMessage;

import java.util.List;

/**
 * Packet used to trigger an action
 */
public interface ITriggerActionMessage extends IRadioMessage {

    /**
     * Callback from the launch event
     *
     * @param statusEvents from invoking the launcher
     */
    default void onTriggerCallback(List<IActionStatus> statusEvents) {

    }
}
