package icbm.classic.api.missiles;

import net.minecraft.entity.Entity;

/**
 * Applied to a missile entity to allow it's rotation and position to be set more cleanly
 */
@Deprecated //TODO replace with something like IProjectileThrowable, where shooter does the init offset calculations and we use the cause system
public interface IMissileAiming
{
    /**
     * Setup position and rotation based on a shooting entity
     *
     * @param shooter - entity firing the missile
     * @param offsetMultiplier - distance offset from shooter to spawn, multiplies the rotation vector
     * @param forceMultiplier - firing velocity of the missile
     */
    default void initAimingPosition(Entity shooter, float offsetMultiplier, float forceMultiplier) {
        initAimingPosition(
            shooter.posX, shooter.posY + (double) shooter.getEyeHeight(), shooter.posZ,
            shooter.rotationYaw, shooter.rotationPitch,
            offsetMultiplier, forceMultiplier
        );
    }

    /**
     * Sets position and rotation
     *
     * @param x - position
     * @param y - position
     * @param z - position
     * @param yaw - aiming
     * @param pitch - aiming
     * @param offsetMultiplier - distance offset from shooter to spawn, multiplies the rotation vector
     * @param forceMultiplier - firing velocity of the missile
     */
    void initAimingPosition(double x, double y, double z, float yaw, float pitch, float offsetMultiplier, float forceMultiplier);
}
