package icbm.classic.api.missiles.parts;

/**
 * Applied to target data that can carry a delay
 */
public interface IMissileTargetDelayed extends IMissileTarget {

    /**
     * Delay to wait before firing the missile at the target. Only used
     * on launch of a missile and ignored for all other systems.
     *
     * @return delay in ticks
     */
    int getFiringDelay();

    /**
     * Creates a copy of the target data without the delay.
     * This is for use in feeding back into the missile during launch. It is recommended
     * to create a new object and not to edit the existing.
     *
     * @return new copy with all settings but delay
     */
    IMissileTarget cloneWithoutDelay();
}
