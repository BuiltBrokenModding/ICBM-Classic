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
}
