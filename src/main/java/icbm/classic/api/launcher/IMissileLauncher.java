package icbm.classic.api.launcher;

import icbm.classic.api.missiles.cause.IMissileCause;
import icbm.classic.api.missiles.parts.IMissileTarget;
import net.minecraft.util.math.Vec3d;

import javax.annotation.Nullable;

/**
 * Capability for accessing data about a launcher
 *
 * Created by Dark(DarkGuardsman, Robin) on 1/9/19.
 */
public interface IMissileLauncher
{

    /**
     * Direct way to get launcher's current status
     *
     * {@link #launch(IMissileTarget, IMissileCause, boolean)} will often invoke this in addition
     * to other logic. This only allows exacting status without triggering pre-checks or launch
     * results. Useful for checking how the launcher itself is doing and not what launcher will do
     * with the missile.
     *
     * @return current status
     */
    IActionStatus getStatus();

    /**
     * Direct way to get launcher's validate result of the target and cause.
     *
     * {@link #launch(IMissileTarget, IMissileCause, boolean)} will often invoke this in addition
     * to other logic. This only allows exacting pre-validation logic directly without worrying
     * about launch results.
     *
     * @return status from pre-checks
     */
    IActionStatus preCheckLaunch(IMissileTarget target, @Nullable IMissileCause cause);



    /**
     * Tries to launch the missile
     *
     * Ensure that simulation logic doesn't trigger any lasting effects on the launcher. As
     * different systems will use simulate as a way to predict launcher behavior. Including
     * seeing what status the launcher will output. As well any issues with firing the missile.
     *
     * @param target to load into missile
     * @param cause to note, optional but recommended to create a history of firing reason
     * @param simulate to do pre-flight checks and get current status
     * @return status of launch
     */
    IActionStatus launch(IMissileTarget target, @Nullable IMissileCause cause, boolean simulate);

    /**
     * Index of the launcher in the network. Used
     * in combination with group to provide indexing
     * for chain fire
     *
     * @return index or -1 to ignore grouping
     */
    default int getLaunchIndex() {
        // TODO future state
        return -1;
    }

    /**
     * Group of the launcher, used for chain firing
     * and limiting which launchers go to which
     * controller.
     *
     * @return index or -1 to ignore grouping
     */
    default int getLauncherGroup() {
        // TODO future state
        return -1;
    }

    /**
     * Gets the predicted inaccuracy for the fire mission
     *
     * @param predictedTarget that might be passed to the launcher
     * @param launchers count being used in the fire mission
     * @return inaccuracy
     */
    default float getInaccuracy(Vec3d predictedTarget, int launchers) {
        return 0;
    }
}
