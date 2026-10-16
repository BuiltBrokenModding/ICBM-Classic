package icbm.classic.lib.transform;

import net.minecraft.util.math.Vec3d;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class RotationHelperTest {

    static Vec3d pos(double x, double y, double z) {
        //abstracting as I will likely change the data type later
        return new Vec3d(x, y, z);
    }
    static Stream<Arguments> rotationYData() {
        return Stream.of(
            // Input vector, input angle, expected output

            // Facing south, rotation nothing, should get south
            Arguments.of(pos(0, 0, 1), 0, pos(0, 0, 1)),
            // Facing south, +90, should get west
            Arguments.of(pos(0, 0, 1), 90, pos(-1, 0, 0)),
            // Facing south, -90, should get east
            Arguments.of(pos(0, 0, 1), -90, pos(1, 0, 0)),
            // Facing south, +180, should get north
            Arguments.of(pos(0, 0, 1), 180, pos(0, 0, -1)),
            // Facing south, -180, should get north
            Arguments.of(pos(0, 0, 1), -180, pos(0, 0, -1)),
            // Facing south, +360, should get south
            Arguments.of(pos(0, 0, 1), 360, pos(0, 0, 1)),
            // Facing south, -360, should get south
            Arguments.of(pos(0, 0, 1), 360, pos(0, 0, 1))
        );
    }
    @ParameterizedTest
    @MethodSource("rotationYData")
    void rotateY(Vec3d vector, double angle, Vec3d expected) {
        Assertions.assertEquals(expected, RotationHelper.rotateY(vector, angle));
    }

    static Stream<Arguments> rotationXData() {
        return Stream.of(
            // Input vector, input angle, expected output

            // Facing south, rotation nothing, should get south
            Arguments.of(pos(0, 0, 1), 0, pos(0, 0, 1)),
            // Facing south, rotation +90, should get down
            Arguments.of(pos(0, 0, 1), 90, pos(0, -1, 0)),
            // Facing south, rotation -90, should get up
            Arguments.of(pos(0, 0, 1), -90, pos(0, 1, 0)),
            // Facing south, rotation -180, should get north
            Arguments.of(pos(0, 0, 1), -180, pos(0, 0, -1)),
            // Facing south, rotation 180, should get north
            Arguments.of(pos(0, 0, 1), -180, pos(0, 0, -1)),
            // Facing south, +360, should get south
            Arguments.of(pos(0, 0, 1), 360, pos(0, 0, 1)),
            // Facing south, -360, should get south
            Arguments.of(pos(0, 0, 1), 360, pos(0, 0, 1))
        );
    }
    @ParameterizedTest
    @MethodSource("rotationXData")
    void rotateX(Vec3d vector, double angle, Vec3d expected) {
        Assertions.assertEquals(expected, RotationHelper.rotateX(vector, angle));
    }

    static Stream<Arguments> rotationZData() {
        return Stream.of(
            // Input vector, input angle, expected output

            // Facing south, rotation nothing, should get south
            Arguments.of(pos(0, 0, 1), 0, pos(0, 0, 1)),
            // Facing south, +90, should get south as we are already facing the axis and thus can't do shit
            Arguments.of(pos(0, 0, 1), +90, pos(0, 0, 1)),
            // Facing south, -90, you will get nothing
            Arguments.of(pos(0, 0, 1), -90, pos(0, 0, 1)),
            // Facing south, -180, not sure what you expected
            Arguments.of(pos(0, 0, 1), -180, pos(0, 0, 1)),
            // Facing south, +180, we both can suffer reading this code
            Arguments.of(pos(0, 0, 1), -180, pos(0, 0, 1)),
            // Facing south, +360, one step
            Arguments.of(pos(0, 0, 1), 360, pos(0, 0, 1)),
            // Facing south, -360, two step... I don't know why I'm testing this
            Arguments.of(pos(0, 0, 1), 360, pos(0, 0, 1))
        );
    }
    @ParameterizedTest
    @MethodSource("rotationZData")
    void rotateZ(Vec3d vector, double angle, Vec3d expected) {
        Assertions.assertEquals(expected, RotationHelper.rotateZ(vector, angle));
    }
}