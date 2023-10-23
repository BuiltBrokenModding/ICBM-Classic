package icbm.classic.lib.data;

import net.minecraft.util.EnumFacing;

import static net.minecraft.util.EnumFacing.*;

public class FaceRotations {

    public static final EnumFacing[] CW_UP_AXIS = new RotationBuilder()
        .start(NORTH)
        .rotation(NORTH, EAST)
        .rotation(EAST, SOUTH)
        .rotation(SOUTH, WEST)
        .rotation(WEST, NORTH)
        .build();

    public static final EnumFacing[] CCW_UP_AXIS = new RotationBuilder()
        .start(NORTH)
        .rotation(NORTH, WEST)
        .rotation(EAST, NORTH)
        .rotation(SOUTH, EAST)
        .rotation(WEST, SOUTH)
        .build();

    public static final EnumFacing[] CW_NORTH_AXIS = new RotationBuilder()
        .start(UP)
        .rotation(UP, WEST)
        .rotation(WEST, DOWN)
        .rotation(DOWN, EAST)
        .rotation(EAST, UP)
        .build();

    public static final EnumFacing[] CW_EAST_AXIS = new RotationBuilder()
        .start(UP)
        .rotation(UP, NORTH)
        .rotation(NORTH, DOWN)
        .rotation(DOWN, SOUTH)
        .rotation(SOUTH, UP)
        .build();

    public static final EnumFacing[] CW_SOUTH_AXIS = new RotationBuilder()
        .start(UP)
        .rotation(UP, EAST)
        .rotation(EAST, DOWN)
        .rotation(DOWN, WEST)
        .rotation(WEST, UP)
        .build();

    public static final EnumFacing[] CW_WEST_AXIS = new RotationBuilder()
        .start(UP)
        .rotation(UP, SOUTH)
        .rotation(SOUTH, DOWN)
        .rotation(DOWN, NORTH)
        .rotation(NORTH, UP)
        .build();

    public static final EnumFacing[] CW_DOWN_AXIS = new RotationBuilder()
        .start(NORTH)
        .rotation(NORTH, WEST)
        .rotation(WEST, SOUTH)
        .rotation(SOUTH, EAST)
        .rotation(EAST, NORTH)
        .build();

    /** Rotates around the face clockwise */
    public static final EnumFacing[][] CW_FACE = new EnumFacing[][] {

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
     *
     * Clockwise {@see https://en.wikipedia.org/wiki/Clockwise}
     *
     * @param face to rotate around
     * @param current rotation
     * @return next rotation, if current is same as face or inverse of face it will pick starting rotation
     */
    public static EnumFacing rotateRight(EnumFacing face, EnumFacing current) {
        return CW_FACE[face.ordinal()][current.ordinal()];
    }

    /**
     * Rotates counter-clockwise, towards left relative to self on a clock face
     *
     * Clockwise {@see https://en.wikipedia.org/wiki/Clockwise}
     *
     * @param face to rotate around
     * @param current rotation
     * @return next rotation, if current is same as face or inverse of face it will pick starting rotation
     */
    public static EnumFacing rotateLeft(EnumFacing face, EnumFacing current) {
        return CW_FACE[face.ordinal()][current.ordinal()].getOpposite();
    }

    public static class RotationBuilder {
        private final EnumFacing[] rotations = new EnumFacing[6];

        public RotationBuilder() {}
        public RotationBuilder(EnumFacing[] sides) {
            System.arraycopy(sides, 0, rotations, 0, 6);
        }

        public RotationBuilder rotation(EnumFacing input, EnumFacing output) {
            rotations[input.ordinal()] = output;
            return this;
        }

        public RotationBuilder start(EnumFacing face) {
            for(int i = 0; i < 6; i++) {
                this.rotations[i] = face;
            }
            return this;
        }

        public EnumFacing[] build() {
            final EnumFacing[] rotations = new EnumFacing[6];
            System.arraycopy(this.rotations, 0, rotations, 0, 6);
            return rotations;
        }
    }
}
