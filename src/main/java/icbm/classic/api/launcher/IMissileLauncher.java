package icbm.classic.api.launcher;

import icbm.classic.api.missiles.cause.IMissileCause;
import icbm.classic.api.missiles.parts.IMissileTarget;

import javax.annotation.Nullable;

/**
 * Capability for accessing data about a launcher
 *
 * Created by Dark(DarkGuardsman, Robin) on 1/9/19.
 */
public interface IMissileLauncher
{

    /**
     * Tries to launch the missile
     *
     * @param target to load into missile
     * @param cause to note, optional but recommended to create a history of firing reason
     * @param simulate to do pre-flight checks and get current status
     * @return status of launch
     */
    IMissileLauncherStatus launch(IMissileTarget target, @Nullable IMissileCause cause, boolean simulate); //TODO add object for trigger reason to wrapper more data
}
