package icbm.classic.lib;

import java.util.Random;

/**
 * Helpers for running common calculations
 * <p>
 * Created by Dark(DarkGuardsman, Robert) on 3/1/2020.
 */
public final class CalculationHelpers {
    private CalculationHelpers() {
        //Empty to prevent creating this class
    }

    /**
     * Generates a random float inside the range defined by -scale to +scale
     *
     * @param rand  random supplied
     * @param scale plus minus scale to use for the range
     * @return random float
     */
    public static float randFloatRange(Random rand, float scale) {
        return randFloatRange(rand, -scale, scale);
    }

    /**
     * Generates a random float inside the range
     *
     * @param rand random supplied
     * @param min  lower bound
     * @param max  upper bound
     * @return random float
     */
    public static float randFloatRange(Random rand, float min, float max) {
        return (rand.nextFloat() * (max - min)) + min;
    }

    /**
     * Generates a random double inside the range defined by -scale to +scale
     *
     * @param rand  random supplied
     * @param scale plus minus scale to use for the range
     * @return random double
     */
    public static double randDoubleRange(Random rand, double scale) {
        return randDoubleRange(rand, -scale, scale);
    }

    /**
     * Generates a random double inside the range
     *
     * @param rand random supplied
     * @param min  lower bound
     * @param max  upper bound
     * @return random double
     */
    public static double randDoubleRange(Random rand, double min, double max) {
        return (rand.nextDouble() * (max - min)) + min;
    }
}
