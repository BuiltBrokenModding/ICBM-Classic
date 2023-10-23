package icbm.classic.lib.radio.messages;

import icbm.classic.api.radio.messages.ITargetMessage;
import icbm.classic.api.radio.messages.ITriggerActionMessage;
import lombok.Data;
import net.minecraft.util.math.Vec3d;

@Data
public class TriggerActionTargetMessage implements ITriggerActionMessage, ITargetMessage {

    private final String channel;
    private final Vec3d target;
}
