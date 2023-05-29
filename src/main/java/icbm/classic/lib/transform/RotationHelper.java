package icbm.classic.lib.transform;

import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

/**
 * Rotation helpers for MC game world
 *
 * MC uses xyz, with x being east-west, y being up-down, and z being north-south
 *
 * {@link net.minecraft.util.EnumFacing}
 *
 * up is +Y (0,1,0)
 * down is -y (0,-1,0)
 * east is +x (1,0,0)
 * west is -x (-1,0,0)
 * north is -z (0,0,-1)
 * south is +z (0,0,1)
 *
 * yaw & pitch both being zero means south, roll as far as I can tell isn't used by the game world. Angles
 * are based off player F3 data. Which matches behavior of entities minus projectiles... which seems to be inverted vector
 * for rotations.
 *
 * -90 pitch is facing up
 * +90 pitch is facing down
 * -90 yaw is facing east
 * +90 yaw is facing west
 *
 * MC seems to use clockwise rotations, this can be understood by picturing yourself moving around a stick
 *
 * pitch, x-axis, would be a stick at waist level. Moving clockwise, aka positive, would mean rotating forward and towards the ground
 *
 * yaw, y-axis, would be a stick or better thought of as a chair. Moving positive would be turning right towards west.
 *
 * roll, z-axis, would be a stick glued to your head. Moving positive rotate your head right while still facing south.
 *
 */
public class RotationHelper {

    /**
     * Rotates a vector around the y-axis by the angle given
     *
     * @param vector to transform
     * @param yaw to rotate with
     * @return new angle
     */
    public static Vec3d rotateY(Vec3d vector, double yaw) { //TODO cache sin/cos for performance
        /* https://mathworld.wolfram.com/RotationMatrix.html
         * cos(a)   0     -sin(a)
         * 0        1           0
         * sin(a)  0      cos(a)
         */
        final double rad = Math.toRadians(clamp360(yaw));
        return new Vec3d(
            fixRounding(vector.x * Math.cos(rad) + vector.z * -Math.sin(rad), 0.001),  //TODO see why x has to be inverted for it to work in MC
            vector.y,
            fixRounding(vector.x * Math.sin(rad) + vector.z * Math.cos(rad), 0.001)
        );
    }


    /**
     * Rotates a vector around the x-axis by the angle given
     *
     * @param vector to transform
     * @param pitch to rotate with
     * @return new angle
     */
    public static Vec3d rotateX(Vec3d vector, double pitch) { //TODO cache sin/cos for performance
        /* https://mathworld.wolfram.com/RotationMatrix.html
        * a is flipped so sin is flipped
         * 1      0               0
         * 0      cos(a)    -sin(a)
         * 0      sin(a)     cos(a)
         */
        final double theta = Math.toRadians(clamp360(pitch));
        return new Vec3d(
            vector.x,
            fixRounding(vector.y * Math.cos(theta) + vector.z * -Math.sin(theta), 0.001),
            fixRounding(vector.y * Math.sin(theta) + vector.z * Math.cos(theta), 0.001)
        );
    }

    /**
     * Rotates a vector around the z-axis by the angle given
     *
     * MC doesn't use roll so clockwise is assumed from the player's perspective, meaning we rotate towards west
     *
     * @param vector to transform
     * @param roll to rotate with
     * @return new vector
     */
    public static Vec3d rotateZ(final Vec3d vector, final double roll) { //TODO cache sin/cos for performance
        /* https://mathworld.wolfram.com/RotationMatrix.html
         * cos(a)  sin(a)  0
         * -sin(a)  cos(a)   0
         *      0       0   1
         */
        final double rad = Math.toRadians(clamp360(roll));
        return new Vec3d(
            fixRounding(vector.x * Math.cos(rad) + vector.y * Math.sin(rad), 0.001),
            fixRounding(vector.x * -Math.sin(rad) + vector.y * Math.cos(rad), 0.001),
            vector.z
        );
    }

    public static double clamp360(double angle) {
        return MathHelper.wrapDegrees(angle);
    }

    public static double fixRounding(double value, double limit) {
        if(Math.abs(value) <= limit) {
            return 0;
        }
        return value;
    }
}
