package icbm.classic.lib.radio.messages;

import icbm.classic.api.radio.messages.ITargetMessage;
import lombok.Data;
import net.minecraft.world.phys.Vec3;

@Data
public class TargetMessage implements ITargetMessage {
    private final String channel;
    private final Vec3 target;
}
