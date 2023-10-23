package icbm.classic.lib.radio.messages;

import icbm.classic.api.radio.messages.ITriggerActionMessage;
import lombok.Data;

@Data
public class TriggerActionMessage implements ITriggerActionMessage {

    private final String channel;
}
