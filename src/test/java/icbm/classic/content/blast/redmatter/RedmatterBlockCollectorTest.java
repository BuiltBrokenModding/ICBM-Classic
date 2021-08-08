package icbm.classic.content.blast.redmatter;

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
    @Nested
    @DisplayName("Tests#CollectBlocksOnWall(size,face,consumer)")
    public class CollectBlocksOnWall {

        @Test
        void north() {
            final List<BlockPos> posList = new ArrayList();
            RedmatterBlockCollector.collectBlocksOnWall(1, EnumFacing.NORTH, (x, y, z) -> {
                posList.add(new BlockPos(x, y, z));
            });

            final List<BlockPos> expected = new ArrayList();
            expected.add(new BlockPos(-1, -1, -1));
            expected.add(new BlockPos(0, -1, -1));
            expected.add(new BlockPos(1, -1, -1));

            expected.add(new BlockPos(-1, 0, -1));
            expected.add(new BlockPos(0, 0, -1));
            expected.add(new BlockPos(1, 0, -1));

            expected.add(new BlockPos(-1, 1, -1));
            expected.add(new BlockPos(0, 1, -1));
            expected.add(new BlockPos(1, 1, -1));
            Assertions.assertEquals(posList, expected);
        }

        @Test
        void south() {
            final List<BlockPos> posList = new ArrayList();
            RedmatterBlockCollector.collectBlocksOnWall(1, EnumFacing.SOUTH, (x, y, z) -> {
                posList.add(new BlockPos(x, y, z));
            });

            final List<BlockPos> expected = new ArrayList();
            expected.add(new BlockPos(-1, -1, 1));
            expected.add(new BlockPos(0, -1, 1));
            expected.add(new BlockPos(1, -1, 1));

            expected.add(new BlockPos(-1, 0, 1));
            expected.add(new BlockPos(0, 0, 1));
            expected.add(new BlockPos(1, 0, 1));

            expected.add(new BlockPos(-1, 1, 1));
            expected.add(new BlockPos(0, 1, 1));
            expected.add(new BlockPos(1, 1, 1));
            Assertions.assertEquals(posList, expected);
        }

        @Test
        void east() {
            final List<BlockPos> posList = new ArrayList();
            RedmatterBlockCollector.collectBlocksOnWall(1, EnumFacing.EAST, (x, y, z) -> {
                posList.add(new BlockPos(x, y, z));
            });

            final List<BlockPos> expected = new ArrayList();
            expected.add(new BlockPos(1, -1, -1));
            expected.add(new BlockPos(1, -1, 0));
            expected.add(new BlockPos(1, -1, 1));

            expected.add(new BlockPos(1, 0, -1));
            expected.add(new BlockPos(1, 0, 0));
            expected.add(new BlockPos(1, 0, 1));

            expected.add(new BlockPos(1, 1, -1));
            expected.add(new BlockPos(1, 1, 0));
            expected.add(new BlockPos(1, 1, 1));
            Assertions.assertEquals(posList, expected);
        }

        @Test
        void west() {
            final List<BlockPos> posList = new ArrayList();
            RedmatterBlockCollector.collectBlocksOnWall(1, EnumFacing.WEST, (x, y, z) -> {
                posList.add(new BlockPos(x, y, z));
            });

            final List<BlockPos> expected = new ArrayList();
            expected.add(new BlockPos(-1, -1, -1));
            expected.add(new BlockPos(-1, -1, 0));
            expected.add(new BlockPos(-1, -1, 1));

            expected.add(new BlockPos(-1, 0, -1));
            expected.add(new BlockPos(-1, 0, 0));
            expected.add(new BlockPos(-1, 0, 1));

            expected.add(new BlockPos(-1, 1, -1));
            expected.add(new BlockPos(-1, 1, 0));
            expected.add(new BlockPos(-1, 1, 1));
            Assertions.assertEquals(posList, expected);
        }

        @Test
        void up() {
            final List<BlockPos> posList = new ArrayList();
            RedmatterBlockCollector.collectBlocksOnWall(1, EnumFacing.UP, (x, y, z) -> {
                posList.add(new BlockPos(x, y, z));
            });

            final List<BlockPos> expected = new ArrayList();
            expected.add(new BlockPos(-1, 1, -1));
            expected.add(new BlockPos(-1, 1, 0));
            expected.add(new BlockPos(-1, 1, 1));

            expected.add(new BlockPos(0, 1, -1));
            expected.add(new BlockPos(0, 1, 0));
            expected.add(new BlockPos(0, 1, 1));

            expected.add(new BlockPos(1, 1, -1));
            expected.add(new BlockPos(1, 1, 0));
            expected.add(new BlockPos(1, 1, 1));
            Assertions.assertEquals(posList, expected);
        }

        @Test
        void down() {
            final List<BlockPos> posList = new ArrayList();
            RedmatterBlockCollector.collectBlocksOnWall(1, EnumFacing.DOWN, (x, y, z) -> {
                posList.add(new BlockPos(x, y, z));
            });

            final List<BlockPos> expected = new ArrayList();
            expected.add(new BlockPos(-1, -1, -1));
            expected.add(new BlockPos(-1, -1, 0));
            expected.add(new BlockPos(-1, -1, 1));

            expected.add(new BlockPos(0, -1, -1));
            expected.add(new BlockPos(0, -1, 0));
            expected.add(new BlockPos(0, -1, 1));

            expected.add(new BlockPos(1, -1, -1));
            expected.add(new BlockPos(1, -1, 0));
            expected.add(new BlockPos(1, -1, 1));
            Assertions.assertEquals(posList, expected);
        }
    }
}
