package icbm.classic.api.radio.messages;

import icbm.classic.api.launcher.IMissileLauncherStatus;
import icbm.classic.api.radio.IRadioMessage;
import icbm.classic.lib.capability.launcher.data.LauncherStatus;
import net.minecraft.util.math.Vec3d;

import java.util.List;
import java.util.function.Consumer;

/**
 * Packet containing targetting information
 */
public interface ILaunchMessage extends IRadioMessage {

    /**
     * Callback from the launch event
     *
     * @param statusEvents from invoking the launcher
     */
    void onLaunchCallback(List<IMissileLauncherStatus> statusEvents);
}
