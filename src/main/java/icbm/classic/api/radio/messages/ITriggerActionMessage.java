package icbm.classic.api.radio.messages;

import icbm.classic.api.actions.status.IActionStatus;
import icbm.classic.api.radio.IRadioMessage;

/**
 * Packet used to trigger an action
 * @deprecated replace with {@link icbm.classic.api.actions.data.IActionFieldProvider} to avoid
 * needing different interfaces for each version. When we can just pass fields.
 */
public interface ITriggerActionMessage extends IRadioMessage {

    /**
     * Callback from the launch event
     *
     * @param status from invoking the launcher
     */
    default void onTriggerCallback(IActionStatus status) {

    }

    /**
     * Should the action be triggered. Some
     * messages may be multipurpose and not always
     * want to trigger the action.
     *
     * @return true if should trigger
     */
    default boolean shouldTrigger() {
        return true;
    }
}
// PORTED
