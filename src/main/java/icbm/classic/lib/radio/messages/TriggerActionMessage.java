package icbm.classic.lib.radio.messages;

import icbm.classic.api.launcher.IActionStatus;
import icbm.classic.api.radio.messages.ITriggerActionMessage;
import lombok.Data;

import java.util.List;

@Data
public class TriggerActionMessage implements ITriggerActionMessage {

    private final String channel;
}
