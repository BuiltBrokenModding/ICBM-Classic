package icbm.classic.content.blast;

import icbm.classic.world.blast.BlastHelpers;
import net.minecraft.util.math.Vec3i;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Dark(DarkGuardsman, Robert) on 3/29/2020.
 */
public class BlastHelpersTest
{
    @Nested
    @DisplayName("forEachPosInRadius")
    class ForEachPosInRadiusGroup
    {
        @Test
        @DisplayName("3 radius")
        void loop3x3()
        {
            final List<Vec3i> positions = new ArrayList<>(7);

            //1 will result in -1 to 1 or a size of 3
            BlastHelpers.forEachPosInRadius(1, (x, y, z) -> {
                positions.add(new Vec3i(x, y, z));
            });

            //Should have 27 points
            Assertions.assertEquals(7, positions.size());

            //Check that each position is valid based on a simple loop
            //  Not the most idea way to validate this but it works
            Assertions.assertEquals(new Vec3i(-1, 0, 0), positions.get(0));
            Assertions.assertEquals(new Vec3i(0, -1, 0), positions.get(1));
            Assertions.assertEquals(new Vec3i(0, 0, -1), positions.get(2));
            Assertions.assertEquals(new Vec3i(0, 0, 0), positions.get(3));
            Assertions.assertEquals(new Vec3i(0, 0, 1), positions.get(4));
            Assertions.assertEquals(new Vec3i(0, 1, 0), positions.get(5));
            Assertions.assertEquals(new Vec3i(1, 0, 0), positions.get(6));
        }
    }

    @Nested
    @DisplayName("forEachPosInRadiusUntil")
    class ForEachPosInRadiusUntilGroup
    {
        @Test
        @DisplayName("stop before first iteration")
        void stopOnFirst()
        {
            final List<Vec3i> positions = new ArrayList<>();
            BlastHelpers.forEachPosInRadiusUntil(1, (x, y, z) -> {
                positions.add(new Vec3i(x, y, z));
                return false; //stop loop
            }, () -> true);

            //Should have a single position
            Assertions.assertEquals(0, positions.size());
        }

        @Test
        @DisplayName("3 radius")
        void loop3x3()
        {
            final List<Vec3i> positions = new ArrayList<>(7);

            //1 will result in -1 to 1 or a size of 3
            BlastHelpers.forEachPosInRadiusUntil(1, (x, y, z) -> {
                positions.add(new Vec3i(x, y, z));
                return true;
            }, () -> false);

            //Should have 27 points
            Assertions.assertEquals(7, positions.size());

            //Check that each position is valid based on a simple loop
            //  Not the most idea way to validate this but it works
            Assertions.assertEquals(new Vec3i(-1, 0, 0), positions.get(0));
            Assertions.assertEquals(new Vec3i(0, -1, 0), positions.get(1));
            Assertions.assertEquals(new Vec3i(0, 0, -1), positions.get(2));
            Assertions.assertEquals(new Vec3i(0, 0, 0), positions.get(3));
            Assertions.assertEquals(new Vec3i(0, 0, 1), positions.get(4));
            Assertions.assertEquals(new Vec3i(0, 1, 0), positions.get(5));
            Assertions.assertEquals(new Vec3i(1, 0, 0), positions.get(6));
        }
    }

    @Nested
    @DisplayName("forEachPosInCube")
    class ForEachPosInCubeTestGroup
    {
        @Test
        @DisplayName("stop after first iteration")
        void stopAfterFirst()
        {
            final List<Vec3i> positions = new ArrayList<>();
            BlastHelpers.forEachPosInCube(1, 1, 1, (x, y, z) -> {
                positions.add(new Vec3i(x, y, z));
                return false; //stop loop
            });

            //Should have a single position
            Assertions.assertEquals(1, positions.size());
            Assertions.assertEquals(new Vec3i(-1, -1, -1), positions.get(0));
        }

        @Test
        @DisplayName("3x3x3 cube - should map all 27 positions in order")
        void loop3x3()
        {
            final List<Vec3i> positions = new ArrayList<>(27);

            //1 will result in -1 to 1 or a size of 3
            BlastHelpers.forEachPosInCube(1, 1, 1, (x, y, z) -> {
                positions.add(new Vec3i(x, y, z));
                return true;
            });

            //Should have 27 points
            Assertions.assertEquals(27, positions.size());

            //Check that each position is valid based on a simple loop
            //  Not the most idea way to validate this but it works
            int index = 0;
            for (int x = -1; x <= 1; x++)
            {
                for (int y = -1; y <= 1; y++)
                {
                    for (int z = -1; z <= 1; z++)
                    {
                        Assertions.assertEquals(new Vec3i(x, y, z), positions.get(index));
                        index++;
                    }
                }
            }
        }
    }
}
