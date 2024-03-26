package icbm.classic.world.blast.redmatter.logic;

import icbm.classic.api.data.Int3Consumer;
import net.minecraft.core.Direction;

/**
 * Created by Robin Seifert on 8/7/2021.
 */
public class RedmatterBlockCollector {
    public static void collectBlocksOnWallEdges(int size, Int3Consumer consumer) {
        //Loop all wall faces
        for (Direction facing : Direction.values()) {
            collectBlocksOnWall(size, facing, consumer);
        }
    }

    public static void collectBlocksOnWall(int size, Direction wall, Int3Consumer consumer) {
        //Loop wall face from negative corner to positive corner (-a,-b) to (+a,+b)
        for (int stepA = -size; stepA <= size; stepA++) {
            for (int stepB = -size; stepB <= size; stepB++) {
                //Offset by wall center point
                int rx = wall.getFrontOffsetX() * size;
                int ry = wall.getFrontOffsetY() * size;
                int rz = wall.getFrontOffsetZ() * size;

                //Offset by step position on the wall based on facing
                if (wall == Direction.DOWN || wall == Direction.UP) {
                    rx += stepA;
                    rz += stepB;
                } else if (wall == Direction.EAST || wall == Direction.WEST) {
                    ry += stepA;
                    rz += stepB;
                } else if (wall == Direction.NORTH || wall == Direction.SOUTH) {
                    ry += stepA;
                    rx += stepB;
                }

                consumer.apply(rx, ry, rz);
            }
        }
    }

}
