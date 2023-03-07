package icbm.classic.lib.radio.messages;

import icbm.classic.api.launcher.IActionStatus;
import icbm.classic.api.radio.messages.ITriggerActionMessage;
import icbm.classic.api.radio.messages.ITargetMessage;
import lombok.Data;
import net.minecraft.util.math.Vec3d;

import java.util.List;

@Data
public class TriggerActionTargetMessage implements ITriggerActionMessage, ITargetMessage {

    private final String channel;
    private final Vec3d target;
}
