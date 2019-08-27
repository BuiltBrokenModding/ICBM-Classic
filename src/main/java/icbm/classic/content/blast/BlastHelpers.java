package icbm.classic.content.blast;

import icbm.classic.api.data.Int3Consumer;

/**
 * Created by Dark(DarkGuardsman, Robert) on 2019-08-27.
 */
public class BlastHelpers
{

    /**
     * Loops a cube with the size given then only returns the values inside the radius
     *
     * @param radius   - xyz size, will ceil then ignore outside
     * @param consumer - callback for the xyz
     */
    public static void loopInRadius(double radius, Int3Consumer consumer)
    {
        loopCubeNP((int) Math.ceil(radius), (x, y, z) ->
        {
            final double radiusSQ = radius * radius;
            final double distanceSQ = x * x + y * y + z * z;
            if (distanceSQ <= radiusSQ)
            {
                consumer.apply(x, y, z);
            }
        });
    }

    /**
     * loops a cube from -size to size
     *
     * @param size - size to loop in the xyz
     */
    public static void loopCubeNP(int size, Int3Consumer consumer)
    {
        loopCubeNP(size, size, size, consumer);
    }

    /**
     * loops a cube from -size to size
     *
     * @param xSize    - size to loop in the x
     * @param ySize    - size to loop in the y
     * @param zSize    - size to loop in the z
     * @param consumer - callback for the xyz
     */
    public static void loopCubeNP(int xSize, int ySize, int zSize, Int3Consumer consumer)
    {
        for (int x = -xSize; x < ySize; x++)
        {
            for (int y = -ySize; y < ySize; y++)
            {
                for (int z = -zSize; z < zSize; z++)
                {
                    consumer.apply(x, y, z);
                }
            }
        }
    }
}
