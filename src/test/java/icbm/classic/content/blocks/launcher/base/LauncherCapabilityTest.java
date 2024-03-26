package icbm.classic.content.blocks.launcher.base;

import icbm.classic.config.machines.ConfigLauncher;
import icbm.classic.world.block.launcher.base.LauncherCapability;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

class LauncherCapabilityTest {

    @ParameterizedTest
    @CsvFileSource(resources = "/data/ballistic_accuracy_tests.csv", numLinesToSkip = 1)
    void calculateInaccuracy(double range, int missiles, float expected) {
        ConfigLauncher.MIN_INACCURACY = 2;
        ConfigLauncher.RANGE = 10000;
        ConfigLauncher.SCALED_INACCURACY_DISTANCE = 10;
        ConfigLauncher.SCALED_INACCURACY_LAUNCHERS = 1;

        final float result = LauncherCapability.calculateInaccuracy(range * range, missiles);
        Assertions.assertEquals(roundToTenth(expected), roundToTenth(result));
    }

    private float roundToTenth(float value) {
        return (int)(value * 10) / 10.0f;
    }
}