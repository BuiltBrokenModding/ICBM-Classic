package icbm.classic.content.blast.redmatter.logic;

import icbm.classic.api.data.Int3Consumer;
import net.minecraft.util.EnumFacing;

/**
 * Created by Robin Seifert on 8/7/2021.
 */
public class RedmatterBlockCollector
{
    public static void collectBlocksOnWallEdges(int size, Int3Consumer consumer)
    {
        //Loop all wall faces
        for (EnumFacing facing : EnumFacing.values())
        {
            collectBlocksOnWall(size, facing, consumer);
        }
    }

    public static void collectBlocksOnWall(int size, EnumFacing wall, Int3Consumer consumer) {
        //Loop wall face from negative corner to positive corner (-a,-b) to (+a,+b)
        for (int stepA = -size; stepA <= size; stepA++)
        {
            for (int stepB = -size; stepB <= size; stepB++)
            {
                //Offset by wall center point
                int rx = wall.getXOffset() * size;
                int ry = wall.getYOffset() * size;
                int rz = wall.getZOffset() * size;

                //Offset by step position on the wall based on facing
                if (wall == EnumFacing.DOWN || wall == EnumFacing.UP)
                {
                    rx += stepA;
                    rz += stepB;
                }
                else if (wall == EnumFacing.EAST || wall == EnumFacing.WEST)
                {
                    ry += stepA;
                    rz += stepB;
                }
                else if (wall == EnumFacing.NORTH || wall == EnumFacing.SOUTH)
                {
                    ry += stepA;
                    rx += stepB;
                }

                consumer.apply(rx, ry, rz);
            }
        }
    }

}
