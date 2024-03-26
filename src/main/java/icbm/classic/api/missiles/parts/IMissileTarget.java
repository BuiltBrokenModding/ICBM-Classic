package icbm.classic.api.missiles.parts;

import net.minecraft.world.phys.Vec3;

/**
 * Abstracted targeting logic for missiles
 */
public interface IMissileTarget extends IBuildableObject {

    /**
     * Target position, assume this is not cached and can change
     *
     * @return current targeting position
     */
    Vec3 getPosition();

    /**
     * Checks if the target data is valid
     */
    boolean isValid();

    /**
     * Getter for x position, assume this is not cached and can change
     *
     * @return double or NaN if no target is set
     */
    double getX();

    /**
     * Getter for y position, assume this is not cached and can change
     *
     * @return double or NaN if no target is set
     */
    double getY();

    /**
     * Getter for z position, assume this is not cached and can change
     *
     * @return double or NaN if no target is set
     */
    double getZ();
}
