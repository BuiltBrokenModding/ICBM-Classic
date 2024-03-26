package icbm.classic.lib.data;

import net.minecraft.core.Direction;

import static net.minecraft.util.Direction.*;

public class FaceRotations {

    public static final Direction[] CW_UP_AXIS = new RotationBuilder()
        .start(NORTH)
        .rotation(NORTH, EAST)
        .rotation(EAST, SOUTH)
        .rotation(SOUTH, WEST)
        .rotation(WEST, NORTH)
        .build();

    public static final Direction[] CCW_UP_AXIS = new RotationBuilder()
        .start(NORTH)
        .rotation(NORTH, WEST)
        .rotation(EAST, NORTH)
        .rotation(SOUTH, EAST)
        .rotation(WEST, SOUTH)
        .build();

    public static final Direction[] CW_NORTH_AXIS = new RotationBuilder()
        .start(UP)
        .rotation(UP, WEST)
        .rotation(WEST, DOWN)
        .rotation(DOWN, EAST)
        .rotation(EAST, UP)
        .build();

    public static final Direction[] CW_EAST_AXIS = new RotationBuilder()
        .start(UP)
        .rotation(UP, NORTH)
        .rotation(NORTH, DOWN)
        .rotation(DOWN, SOUTH)
        .rotation(SOUTH, UP)
        .build();

    public static final Direction[] CW_SOUTH_AXIS = new RotationBuilder()
        .start(UP)
        .rotation(UP, EAST)
        .rotation(EAST, DOWN)
        .rotation(DOWN, WEST)
        .rotation(WEST, UP)
        .build();

    public static final Direction[] CW_WEST_AXIS = new RotationBuilder()
        .start(UP)
        .rotation(UP, SOUTH)
        .rotation(SOUTH, DOWN)
        .rotation(DOWN, NORTH)
        .rotation(NORTH, UP)
        .build();

    public static final Direction[] CW_DOWN_AXIS = new RotationBuilder()
        .start(NORTH)
        .rotation(NORTH, WEST)
        .rotation(WEST, SOUTH)
        .rotation(SOUTH, EAST)
        .rotation(EAST, NORTH)
        .build();

    /**
     * Rotates around the face clockwise
     */
    public static final Direction[][] CW_FACE = new Direction[][]{

        // UP
        CW_UP_AXIS,
        // DOWN
        CW_DOWN_AXIS,
        // North
        CW_NORTH_AXIS,
        // South
        CW_SOUTH_AXIS,
        // West
        CW_WEST_AXIS,
        // East
        CW_EAST_AXIS
    };

    /**
     * Rotates clockwise, towards right relative to self on a clock face
     * <p>
     * Clockwise {@see https://en.wikipedia.org/wiki/Clockwise}
     *
     * @param face    to rotate around
     * @param current rotation
     * @return next rotation, if current is same as face or inverse of face it will pick starting rotation
     */
    public static Direction rotateRight(Direction face, Direction current) {
        return CW_FACE[face.ordinal()][current.ordinal()];
    }

    /**
     * Rotates counter-clockwise, towards left relative to self on a clock face
     * <p>
     * Clockwise {@see https://en.wikipedia.org/wiki/Clockwise}
     *
     * @param face    to rotate around
     * @param current rotation
     * @return next rotation, if current is same as face or inverse of face it will pick starting rotation
     */
    public static Direction rotateLeft(Direction face, Direction current) {
        return CW_FACE[face.ordinal()][current.ordinal()].getOpposite();
    }

    public static class RotationBuilder {
        private final Direction[] rotations = new Direction[6];

        public RotationBuilder() {
        }

        public RotationBuilder(Direction[] sides) {
            System.arraycopy(sides, 0, rotations, 0, 6);
        }

        public RotationBuilder rotation(Direction input, Direction output) {
            rotations[input.ordinal()] = output;
            return this;
        }

        public RotationBuilder start(Direction face) {
            for (int i = 0; i < 6; i++) {
                this.rotations[i] = face;
            }
            return this;
        }

        public Direction[] build() {
            final Direction[] rotations = new Direction[6];
            System.arraycopy(this.rotations, 0, rotations, 0, 6);
            return rotations;
        }
    }
}
