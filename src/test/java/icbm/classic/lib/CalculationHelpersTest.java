package icbm.classic.lib;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Random;

import static org.mockito.Mockito.when;

/**
 * Created by Dark(DarkGuardsman, Robert) on 3/1/2020.
 */
public class CalculationHelpersTest
{
    //Mock random so we can control output
    final Random random = Mockito.mock(Random.class);

    @Test
    void randFloatRange_max()
    {
        //Force to max
        when(random.nextFloat()).thenReturn(1f);

        //Run calc
        final float result = CalculationHelpers.randFloatRange(random, -10, 10);

        //output should be 10
        Assertions.assertEquals(10f, result);
    }

    @Test
    void randFloatRange_min()
    {
        //Force to max
        when(random.nextFloat()).thenReturn(0f);

        //Run calc
        final float result = CalculationHelpers.randFloatRange(random, -10, 10);

        //output should be -10
        Assertions.assertEquals(-10f, result);
    }

    @Test
    void randFloatRange_staysInRange()
    {
        final Random normalRandom = new Random();

        for(int i = 0; i < 100; i++)
        {
            //Run calc
            final float result = CalculationHelpers.randFloatRange(normalRandom, 3.3f);

            //output should be in range
            Assertions.assertTrue(result <= 3.3f, result + " is not less than or equal to 3.3f");
            Assertions.assertTrue(result >= -3.3f, result + " is not great than or equal to -3.3f");
        }
    }

    @Test
    void randDoubleRange_max()
    {
        //Force to max
        when(random.nextDouble()).thenReturn(1d);

        //Run calc
        final double result = CalculationHelpers.randDoubleRange(random, -10, 10);

        //output should be 10
        Assertions.assertEquals(10d, result);
    }

    @Test
    void randDoubleRange_min()
    {
        //Force to max
        when(random.nextDouble()).thenReturn(0d);

        //Run calc
        final double result = CalculationHelpers.randDoubleRange(random, -10, 10);

        //output should be -10
        Assertions.assertEquals(-10d, result);
    }

    @Test
    void randDoubleRange_staysInRange()
    {
        final Random normalRandom = new Random();

        for(int i = 0; i < 100; i++)
        {
            //Run calc
            final double result = CalculationHelpers.randDoubleRange(normalRandom, 3.3d);

            //output should be in range
            Assertions.assertTrue(result <= 3.3d, result + " is not less than or equal to 3.3d");
            Assertions.assertTrue(result >= -3.3d, result + " is not great than or equal to -3.3d");
        }
    }
}
