package icbm.classic.content.blocks.emptower;

import icbm.classic.DummyMultiTile;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

/**
 * Created by Dark(DarkGuardsman, Robert) on 12/15/2019.
 */
public class TestTileEMPTower
{
    @Test
    void testGetLayoutOfMultiBlock_containsLayout()
    {
        final TileEMPTower tileEMPTower = new TileEMPTower();
        final List<BlockPos> list = tileEMPTower.getLayoutOfMultiBlock();

        //Should only provide 1 block
        Assertions.assertEquals(list.size(), 1);

        //Should only provide a block above
        Assertions.assertTrue(list.contains(new BlockPos(0, 1, 0)));
    }

    private static Stream<Arguments> provideMultiBlockContainCases()
    {
        return Stream.of(
                Arguments.of(new BlockPos(0, 1, 0), true),
                Arguments.of(new BlockPos(0, -1, 0), false),
                Arguments.of(new BlockPos(1, 0, 0), false),
                Arguments.of(new BlockPos(-1, 0, 0), false),
                Arguments.of(new BlockPos(0, 0, 1), false),
                Arguments.of(new BlockPos(1, 0, -1), false),
                Arguments.of(new BlockPos(1, 1, 1), false),
                Arguments.of(new BlockPos(-1, -1, -1), false)
        );
    }

    @ParameterizedTest
    @MethodSource("provideMultiBlockContainCases")
    void testMultiBlockContains(BlockPos pos, boolean expected)
    {
        final BlockPos center = new BlockPos(20, 30, 40);
        //Create tower
        final TileEMPTower tileEMPTower = new TileEMPTower();
        tileEMPTower.setPos(center);

        //Create mutli-block
        final DummyMultiTile tileMulti = new DummyMultiTile();
        tileMulti.setPos(center.add(pos));

        //Run test
        Assertions.assertEquals(tileEMPTower.multiBlockContains(tileMulti), expected);
    }

    @Test
    void testMultiTileAdded_addsBlock()
    {
        //Create tower
        final TileEMPTower tileEMPTower = new TileEMPTower();
        tileEMPTower.setPos(new BlockPos(20, 30, 40));

        //Create mutli-block 1 above
        final DummyMultiTile tileMulti = new DummyMultiTile();
        tileMulti.setPos(new BlockPos(20, 31, 40));

        //Invoke method
        tileEMPTower.onMultiTileAdded(tileMulti);

        //Check that we set host
        Assertions.assertEquals(tileEMPTower, tileMulti.getHost());
    }

    @Test
    void testMultiTileAdded_ignoresBlock()
    {
        //Create tower
        final TileEMPTower tileEMPTower = new TileEMPTower();
        tileEMPTower.setPos(new BlockPos(20, 30, 40));

        //Create mutli-block 1 below
        final DummyMultiTile tileMulti = new DummyMultiTile();
        tileMulti.setPos(new BlockPos(20, 29, 40));

        //Invoke method
        tileEMPTower.onMultiTileAdded(tileMulti);

        //Check that we set host
        Assertions.assertNull(tileMulti.getHost());
    }

    @Test
    void testMultiTileBroken_containsBlock()
    {
        //Create tower
        final TileEMPTower tileEMPTower = new TileEMPTower();
        tileEMPTower.setPos(new BlockPos(20, 30, 40));

        //Create mutli-block 1 above
        final DummyMultiTile tileMulti = new DummyMultiTile();
        tileMulti.setPos(new BlockPos(20, 31, 40));

        //Invoke method
        Assertions.assertTrue(tileEMPTower.onMultiTileBroken(tileMulti, null, true));
    }

    @Test
    void testMultiTileBroken_ignoresBlock()
    {
        //Create tower
        final TileEMPTower tileEMPTower = new TileEMPTower();
        tileEMPTower.setPos(new BlockPos(20, 30, 40));

        //Create mutli-block 1 above
        final DummyMultiTile tileMulti = new DummyMultiTile();
        tileMulti.setPos(new BlockPos(20, 29, 40));

        //Invoke method
        Assertions.assertFalse(tileEMPTower.onMultiTileBroken(tileMulti, null, true));
    }
}
