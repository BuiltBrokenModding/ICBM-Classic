package icbm.classic.content.blocks.emptower;

import icbm.classic.api.EnumTier;
import icbm.classic.api.explosion.responses.BlastResponse;
import icbm.classic.api.refs.ICBMExplosives;
import icbm.classic.api.explosion.BlastState;
import icbm.classic.api.explosion.IBlast;
import icbm.classic.api.explosion.IBlastFactory;
import icbm.classic.content.blast.Blast;
import icbm.classic.content.blast.BlastEMP;
import icbm.classic.lib.explosive.reg.ExplosiveData;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * Created by Dark(DarkGuardsman, Robert) on 12/15/2019.
 */
public class TestTileEMPTower
{
    final World world = Mockito.mock(World.class);

    //Helper to create the tower tile
    private TileEMPTower create() {
        TileEMPTower tileEntity = new TileEMPTower();
        tileEntity.setWorld(world);
        return tileEntity;
    }

    @BeforeAll
    public static void setupForAllTests() {
        final ResourceLocation name = new ResourceLocation("ICBM:EMP");
        final IBlastFactory factory = () -> new BlastEMP().setEffectBlocks().setEffectEntities().setBlastSize(50);
        ICBMExplosives.EMP = new ExplosiveData(name,16,EnumTier.THREE).blastFactory(factory);
    }

    @AfterAll
    public static void tearDownForAllTests() {
        ICBMExplosives.EMP = null;
    }

    /*
    @Test
    void testGetLayoutOfMultiBlock_containsLayout()
    {
        final TileEMPTower tileEMPTower = create();
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
        final TileEMPTower tileEMPTower = create();
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
        final TileEMPTower tileEMPTower = create();
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
        final TileEMPTower tileEMPTower = create();
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
        final TileEMPTower tileEMPTower = create();
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
        final TileEMPTower tileEMPTower = create();
        tileEMPTower.setPos(new BlockPos(20, 30, 40));

        //Create mutli-block 1 above
        final DummyMultiTile tileMulti = new DummyMultiTile();
        tileMulti.setPos(new BlockPos(20, 29, 40));

        //Invoke method
        Assertions.assertFalse(tileEMPTower.onMultiTileBroken(tileMulti, null, true));
    }*/

    @Test
    void testGenerateEmp_all()
    {
        //Create tower
        final TileEMPTower tileEMPTower = create();
        tileEMPTower.setPos(new BlockPos(20, 30, 40));

        //Create EMP
        final IBlast emp = tileEMPTower.buildBlast();

        //Validate position
        Assertions.assertEquals(20.5, emp.x());
        Assertions.assertEquals(31.2, emp.y());
        Assertions.assertEquals(40.5, emp.z());

        //Validate world
        Assertions.assertEquals(world, emp.world());

        //Validate size
        Assertions.assertEquals(tileEMPTower.range, emp.getBlastRadius());
    }

    @Test
    void testFire_isReady_hasEnergy()
    {
        //Create tower, create mock around tile so we can fake some methods
        final TileEMPTower tileEMPTower = spy(create());
        tileEMPTower.setPos(new BlockPos(20, 30, 40));
        tileEMPTower.energyStorage.withOnChange(null);
        tileEMPTower.energyStorage.setEnergyStored(Integer.MAX_VALUE);

        //Mock blast so we don't invoke world calls
        when(tileEMPTower.buildBlast()).thenReturn(new Blast()
        {
            @Override
            public BlastResponse runBlast()
            {
                return BlastState.TRIGGERED.genericResponse;
            }
        });

        //Should have fired
        Assertions.assertTrue(tileEMPTower.fire());
    }

    @Test
    void testFire_isReady_lacksEnergy()
    {
        //Create tower, create mock around tile so we can fake some methods
        final TileEMPTower tileEMPTower = create();
        tileEMPTower.setPos(new BlockPos(20, 30, 40));
        tileEMPTower.energyStorage.withOnChange(null);
        tileEMPTower.energyStorage.setEnergyStored(0);

        //Should have fired
        Assertions.assertFalse(tileEMPTower.fire());
    }

    @Test
    void testFire_notReady_lacksEnergy()
    {
        //Create tower, create mock around tile so we can fake some methods
        final TileEMPTower tileEMPTower = create();
        tileEMPTower.setPos(new BlockPos(20, 30, 40));
        tileEMPTower.energyStorage.withOnChange(null);
        tileEMPTower.energyStorage.setEnergyStored(0);
        tileEMPTower.cooldownTicks = 1;

        //Should have fired
        Assertions.assertFalse(tileEMPTower.fire());
    }

    @Test
    void testFire_notReady_hasEnergy()
    {
        //Create tower, create mock around tile so we can fake some methods
        final TileEMPTower tileEMPTower = create();
        tileEMPTower.setPos(new BlockPos(20, 30, 40));
        tileEMPTower.energyStorage.withOnChange(null);
        tileEMPTower.energyStorage.setEnergyStored(Integer.MAX_VALUE);
        tileEMPTower.cooldownTicks = 1;

        //Should have fired
        Assertions.assertFalse(tileEMPTower.fire());
    }
}
