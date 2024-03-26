package icbm.classic.lib.transform;

import com.builtbroken.jlib.data.vector.Vec3;
import net.minecraft.core.BlockPos;

import java.util.Comparator;

/**
 * Created by Dark(DarkGuardsman, Robert) on 10/8/2018.
 */
public class PosDistanceSorter implements Comparator<BlockPos> {
    final Vec3 center;
    final boolean sortY;
    final Sort method;

    public PosDistanceSorter(Vec3 center, boolean sortY, Sort method) {
        this.center = center;
        this.sortY = sortY;
        this.method = method;
    }

    @Override
    public int compare(BlockPos o1, BlockPos o2) {
        if (!sortY || o1.getY() == o2.getY()) {
            return Integer.compare(distance(o1), distance(o2));
        }
        return Integer.compare(o1.getY(), o2.getY());
    }

    private int distance(BlockPos point) {
        final int deltaX = Math.abs(center.xi() - point.getX());
        final int deltaY = Math.abs(center.yi() - point.getY());
        final int deltaZ = Math.abs(center.zi() - point.getZ());

        if (method == Sort.MANHATTEN) {
            return deltaX + deltaY + deltaZ;
        } else if (method == Sort.SQRT) {
            return (int) Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);
        }
        return deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ;
    }

    public static enum Sort {
        //https://en.wikipedia.org/wiki/Taxicab_geometry
        MANHATTEN,
        SQ,
        SQRT
    }
}
