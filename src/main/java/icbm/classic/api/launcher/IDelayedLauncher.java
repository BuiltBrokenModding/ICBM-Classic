package icbm.classic.api.launcher;

import icbm.classic.api.missiles.cause.IMissileCause;
import icbm.classic.api.missiles.parts.IMissileTarget;

import javax.annotation.Nullable;

public interface IDelayedLauncher extends IMissileLauncher {
    /**
     * Tries to launch the missile
     *
     * @param target to load into missile
     * @param cause to note, optional but recommended to create a history of firing reason
     * @param simulate to do pre-flight checks and get current status
     * @param delay Delay of the launch, -1 means launcher will use its own delay, higher than 1 it will launch based on the controller
     * @return status of launch
     */
    IActionStatus launch(IMissileTarget target, @Nullable IMissileCause cause, boolean simulate, int delay);
}
