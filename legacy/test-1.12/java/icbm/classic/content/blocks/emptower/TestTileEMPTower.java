package icbm.classic.content.blocks.emptower;

import icbm.classic.api.EnumTier;
import icbm.classic.api.refs.ICBMExplosives;
import icbm.classic.api.explosion.IBlastFactory;
import icbm.classic.lib.actions.status.ActionResponses;
import icbm.classic.content.actions.emp.ActionEmpArea;
import icbm.classic.lib.explosive.reg.ExplosiveData;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
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
        final IBlastFactory factory = (w, x, y, z, s) -> new ActionEmpArea(w, new Vec3d(x, y, z), s, null).setSize(50);
        ICBMExplosives.EMP = new ExplosiveData(name,16, EnumTier.THREE, factory);
    }

    @AfterAll
    public static void tearDownForAllTests() {
        ICBMExplosives.EMP = null;
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
        when(tileEMPTower.empAction.doAction(world, 20, 30, 40, null)).thenReturn(ActionResponses.COMPLETED);

        //Should have fired
        Assertions.assertTrue(tileEMPTower.fire(null));
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
        Assertions.assertFalse(tileEMPTower.fire(null));
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
        Assertions.assertFalse(tileEMPTower.fire(null));
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
        Assertions.assertFalse(tileEMPTower.fire(null));
    }
}
