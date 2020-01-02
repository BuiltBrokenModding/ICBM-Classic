package icbm.classic.lib.transform;

import com.builtbroken.jlib.data.vector.IPos3D;
import icbm.classic.lib.transform.vector.Pos;
import net.minecraft.util.math.BlockPos;

import java.util.Comparator;

/**
 *
 * Created by Dark(DarkGuardsman, Robert) on 10/8/2018.
 */
public class PosDistanceSorter implements Comparator<BlockPos>
{
    final IPos3D center;
    final boolean sortY;

    public PosDistanceSorter(IPos3D center, boolean sortY)
    {
        this.center = center;
        this.sortY = sortY;
    }

    @Override
    public int compare(BlockPos o1, BlockPos o2)
    {
        if (!sortY || o1.getY() == o2.getY())
        {
            double d = new Pos(o1).distance(center);
            double d2 = new Pos(o2).distance(center);
            return d > d2 ? 1 : d == d2 ? 0 : -1;
        }
        return Integer.compare(o1.getY(), o2.getY());
    }
}
