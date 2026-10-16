package icbm.classic.content.blast.redmatter.logic;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Robin Seifert on 8/7/2021.
 */
public class RedmatterBlockCollectorTest
{
    public static final List<BlockPos> ALL_DATA = new ArrayList();
    public static final List<BlockPos> NORTH_DATA = new ArrayList();
    public static final List<BlockPos> SOUTH_DATA = new ArrayList();
    public static final List<BlockPos> EAST_DATA = new ArrayList();
    public static final List<BlockPos> WEST_DATA = new ArrayList();
    public static final List<BlockPos> UP_DATA = new ArrayList();
    public static final List<BlockPos> DOWN_DATA = new ArrayList();

    static {

        NORTH_DATA.add(new BlockPos(-1, -1, -1));
        NORTH_DATA.add(new BlockPos(0, -1, -1));
        NORTH_DATA.add(new BlockPos(1, -1, -1));

        NORTH_DATA.add(new BlockPos(-1, 0, -1));
        NORTH_DATA.add(new BlockPos(0, 0, -1));
        NORTH_DATA.add(new BlockPos(1, 0, -1));

        NORTH_DATA.add(new BlockPos(-1, 1, -1));
        NORTH_DATA.add(new BlockPos(0, 1, -1));
        NORTH_DATA.add(new BlockPos(1, 1, -1));

        //------------------
        SOUTH_DATA.add(new BlockPos(-1, -1, 1));
        SOUTH_DATA.add(new BlockPos(0, -1, 1));
        SOUTH_DATA.add(new BlockPos(1, -1, 1));

        SOUTH_DATA.add(new BlockPos(-1, 0, 1));
        SOUTH_DATA.add(new BlockPos(0, 0, 1));
        SOUTH_DATA.add(new BlockPos(1, 0, 1));

        SOUTH_DATA.add(new BlockPos(-1, 1, 1));
        SOUTH_DATA.add(new BlockPos(0, 1, 1));
        SOUTH_DATA.add(new BlockPos(1, 1, 1));

        //------------------
        EAST_DATA.add(new BlockPos(1, -1, -1));
        EAST_DATA.add(new BlockPos(1, -1, 0));
        EAST_DATA.add(new BlockPos(1, -1, 1));

        EAST_DATA.add(new BlockPos(1, 0, -1));
        EAST_DATA.add(new BlockPos(1, 0, 0));
        EAST_DATA.add(new BlockPos(1, 0, 1));

        EAST_DATA.add(new BlockPos(1, 1, -1));
        EAST_DATA.add(new BlockPos(1, 1, 0));
        EAST_DATA.add(new BlockPos(1, 1, 1));

        //------------------
        WEST_DATA.add(new BlockPos(-1, -1, -1));
        WEST_DATA.add(new BlockPos(-1, -1, 0));
        WEST_DATA.add(new BlockPos(-1, -1, 1));

        WEST_DATA.add(new BlockPos(-1, 0, -1));
        WEST_DATA.add(new BlockPos(-1, 0, 0));
        WEST_DATA.add(new BlockPos(-1, 0, 1));

        WEST_DATA.add(new BlockPos(-1, 1, -1));
        WEST_DATA.add(new BlockPos(-1, 1, 0));
        WEST_DATA.add(new BlockPos(-1, 1, 1));

        //----------------
        UP_DATA.add(new BlockPos(-1, 1, -1));
        UP_DATA.add(new BlockPos(-1, 1, 0));
        UP_DATA.add(new BlockPos(-1, 1, 1));

        UP_DATA.add(new BlockPos(0, 1, -1));
        UP_DATA.add(new BlockPos(0, 1, 0));
        UP_DATA.add(new BlockPos(0, 1, 1));

        UP_DATA.add(new BlockPos(1, 1, -1));
        UP_DATA.add(new BlockPos(1, 1, 0));
        UP_DATA.add(new BlockPos(1, 1, 1));

        //-----------------
        DOWN_DATA.add(new BlockPos(-1, -1, -1));
        DOWN_DATA.add(new BlockPos(-1, -1, 0));
        DOWN_DATA.add(new BlockPos(-1, -1, 1));

        DOWN_DATA.add(new BlockPos(0, -1, -1));
        DOWN_DATA.add(new BlockPos(0, -1, 0));
        DOWN_DATA.add(new BlockPos(0, -1, 1));

        DOWN_DATA.add(new BlockPos(1, -1, -1));
        DOWN_DATA.add(new BlockPos(1, -1, 0));
        DOWN_DATA.add(new BlockPos(1, -1, 1));

        //----------------\
        ALL_DATA.addAll(DOWN_DATA);
        ALL_DATA.addAll(UP_DATA);
        ALL_DATA.addAll(NORTH_DATA);
        ALL_DATA.addAll(SOUTH_DATA);
        ALL_DATA.addAll(WEST_DATA);
        ALL_DATA.addAll(EAST_DATA);
    }


    @Test
    void collectBlocksOnWallEdges_size1() {
        final List<BlockPos> posList = new ArrayList();
        RedmatterBlockCollector.collectBlocksOnWallEdges(1,(x, y, z) -> {
            posList.add(new BlockPos(x, y, z));
        });
        Assertions.assertEquals(posList, ALL_DATA);
    }

    @Nested
    @DisplayName("Tests#CollectBlocksOnWall(size,face,consumer)")
    public class CollectBlocksOnWall {

        @Test
        void north() {
            final List<BlockPos> posList = new ArrayList();
            RedmatterBlockCollector.collectBlocksOnWall(1, EnumFacing.NORTH, (x, y, z) -> {
                posList.add(new BlockPos(x, y, z));
            });
            Assertions.assertEquals(posList, NORTH_DATA);
        }

        @Test
        void south() {
            final List<BlockPos> posList = new ArrayList();
            RedmatterBlockCollector.collectBlocksOnWall(1, EnumFacing.SOUTH, (x, y, z) -> {
                posList.add(new BlockPos(x, y, z));
            });
            Assertions.assertEquals(posList, SOUTH_DATA);
        }

        @Test
        void east() {
            final List<BlockPos> posList = new ArrayList();
            RedmatterBlockCollector.collectBlocksOnWall(1, EnumFacing.EAST, (x, y, z) -> {
                posList.add(new BlockPos(x, y, z));
            });
            Assertions.assertEquals(posList, EAST_DATA);
        }

        @Test
        void west() {
            final List<BlockPos> posList = new ArrayList();
            RedmatterBlockCollector.collectBlocksOnWall(1, EnumFacing.WEST, (x, y, z) -> {
                posList.add(new BlockPos(x, y, z));
            });
            Assertions.assertEquals(posList, WEST_DATA);
        }

        @Test
        void up() {
            final List<BlockPos> posList = new ArrayList();
            RedmatterBlockCollector.collectBlocksOnWall(1, EnumFacing.UP, (x, y, z) -> {
                posList.add(new BlockPos(x, y, z));
            });
            Assertions.assertEquals(posList, UP_DATA);
        }

        @Test
        void down() {
            final List<BlockPos> posList = new ArrayList();
            RedmatterBlockCollector.collectBlocksOnWall(1, EnumFacing.DOWN, (x, y, z) -> {
                posList.add(new BlockPos(x, y, z));
            });
            Assertions.assertEquals(posList, DOWN_DATA);
        }
    }
}
