package icbm.classic.lib.radio.messages;

import icbm.classic.api.launcher.IMissileLauncherStatus;
import icbm.classic.api.radio.messages.ILaunchMessage;
import lombok.Data;

import java.util.List;

@Data
public class LaunchMessage implements ILaunchMessage {

    private final String channel;

    @Override
    public void onLaunchCallback(List<IMissileLauncherStatus> statusEvents) {
        //NOOP
    }
}
