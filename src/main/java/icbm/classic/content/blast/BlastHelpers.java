package icbm.classic.content.blast;

import icbm.classic.api.data.Int3Consumer;
import icbm.classic.api.data.Int3Looper;

import javax.annotation.Nonnull;
import java.util.function.BooleanSupplier;

/**
 * Created by Dark(DarkGuardsman, Robert) on 2019-08-27.
 */
public final class BlastHelpers
{
    private BlastHelpers()
    {
        //Empty as this is a helper class only
    }

    /**
     * Loops a cube with the size given then only returns the values inside the radius
     *
     * @param radius   - xyz size, will ceil then ignore outside
     * @param consumer - callback for the xyz
     */
    public static void forEachPosInRadius(double radius, Int3Consumer consumer)
    {
        forEachPosInRadius(radius, (x, y, z) -> {
            consumer.apply(x, y, z);
            return true;
        }, () -> false);
    }

    /**
     * Loops a cube with the size given then only returns the values inside the radius
     *
     * @param radius   - xyz size, will ceil then ignore outside
     * @param consumer - callback for the xyz
     */
    public static void forEachPosInRadius(double radius, Int3Looper consumer, BooleanSupplier stopper)
    {
        final int size = (int) Math.ceil(radius);
        forEachPosInCube(size, size, size, (x, y, z) ->
        {
            //Check if we should stop
            if (stopper.getAsBoolean())
            {
                return false;
            }

            final double radiusSQ = radius * radius;
            final double distanceSQ = x * x + y * y + z * z;
            if (distanceSQ <= radiusSQ)
            {
                return consumer.apply(x, y, z);
            }
            return true;
        });
    }

    /**
     * loops a cube from -size to size
     * <p>
     * If a value of (1x, 1y, 1z) is provided the output cube will be 3x3x3. As
     * it will go from -1 to 1 in each axis.
     *
     * @param xSize    - size to loop in the x
     * @param ySize    - size to loop in the y
     * @param zSize    - size to loop in the z
     * @param consumer - callback for the xyz, returning false in the callback will cancel the loop
     */
    public static void forEachPosInCube(int xSize, int ySize, int zSize, @Nonnull Int3Looper consumer)
    {
        for (int x = -xSize; x <= xSize; x++)
        {
            for (int y = -ySize; y <= ySize; y++)
            {
                for (int z = -zSize; z <= zSize; z++)
                {
                    if (!consumer.apply(x, y, z))
                    {
                        return;
                    }
                }
            }
        }
    }
}
