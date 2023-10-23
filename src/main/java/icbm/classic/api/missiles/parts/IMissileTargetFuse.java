package icbm.classic.api.missiles.parts;

/**
 * Applied to target data that can carry fuse settings
 */
public interface IMissileTargetFuse extends IMissileTarget {

    /**
     * Delay to wait before triggering the payload
     *
     * @return delay in ticks
     */
    int getImpactDelay(); //TODO implement
}
