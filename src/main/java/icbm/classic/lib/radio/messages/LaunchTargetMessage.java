package icbm.classic.lib.radio.messages;

import icbm.classic.api.launcher.IMissileLauncherStatus;
import icbm.classic.api.radio.messages.ILaunchMessage;
import icbm.classic.api.radio.messages.ITargetMessage;
import lombok.Data;
import net.minecraft.util.math.Vec3d;

import java.util.List;

@Data
public class LaunchTargetMessage implements ILaunchMessage, ITargetMessage {

    private final String channel;
    private final Vec3d target;

    @Override
    public void onLaunchCallback(List<IMissileLauncherStatus> statusEvents) {
        //NOOP
    }
}
