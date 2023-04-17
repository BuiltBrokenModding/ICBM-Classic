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
     * Tries to launch the missile
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
