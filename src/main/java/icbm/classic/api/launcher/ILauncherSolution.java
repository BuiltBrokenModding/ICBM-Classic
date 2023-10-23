package icbm.classic.api.launcher;

import icbm.classic.api.missiles.parts.IMissileTarget;

/**
 * Firing solution to provide target and additional settings
 */
public interface ILauncherSolution {

    /**
     * Target data to feed into the missile during launch
     *
     * This may be called several times for the same launcher. Ensure
     * that it produces the same results each call. If randomization
     * is used, store the randomization on solution creation.
     *
     * @param launcher to allow selectively applying target
     * @return target data to use
     */
    IMissileTarget getTarget(IMissileLauncher launcher);

    /**
     * Number of missiles to fire, used for multi-launchers
     * or launcher networks. Single launchers may not
     * support group selection.
     *
     * @return 0 or greater, -1 to fire all
     */
    default int getFiringCount() {
        return 1;
    }

    /**
     * Group of missiles to fire, used for multi-launchers
     * or launcher networks. Single launchers may not
     * support group selection.
     *
     * @return 0 or group to match firing group
     */
    default int getFiringGroup() {
        return -1;
    }
}
