package icbm.classic.content.missile.entity.explosive;

import com.adelean.inject.resources.junit.jupiter.GivenJsonResource;
import com.adelean.inject.resources.junit.jupiter.TestWithResources;
import icbm.classic.ICBMClassic;
import icbm.classic.TestBase;
import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.caps.IExplosive;
import icbm.classic.content.items.ItemMissile;
import icbm.classic.content.missile.logic.flight.DeadFlightLogic;
import icbm.classic.content.missile.logic.source.ActionSource;
import icbm.classic.content.missile.logic.source.cause.EntityCause;
import icbm.classic.content.reg.ItemReg;
import icbm.classic.lib.projectile.InGroundData;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import org.junit.jupiter.api.*;

import java.util.UUID;

@TestWithResources
public class EntityExplosiveMissileTest extends TestBase
{
    @GivenJsonResource("data/saves/4.2.0/entity_missile_rpg.json")
    NBTTagCompound rpgSave;

    @BeforeAll
    public static void beforeAllTests()
    {
        // Register missile for loading explosive stack from save
        ForgeRegistries.ITEMS.register(ItemReg.itemExplosiveMissile = new ItemMissile().setName("explosive_missile").setCreativeTab(ICBMClassic.CREATIVE_TAB));
    }

    @Test
    @DisplayName("Loaded file saves back to same tag set")
    void readMatchesWrite() {
        final World world = testManager.getWorld();
        final EntityExplosiveMissile missile = new EntityExplosiveMissile(world);

        // Load entity custom save
        missile.readEntityFromNBT(rpgSave);

        final NBTTagCompound newSave = new NBTTagCompound();
        missile.writeEntityToNBT(newSave);

        // Validate our new save matches the inputted data
        // Only checking for deltas in save data, doesn't confirm save/load is good... only not fucked
        assertTags(rpgSave, newSave);
    }

    @Test
    @DisplayName("Loads rpg missile from existing save")
    void loadFromSave_rpg() {
        final World world = testManager.getWorld();
        final EntityExplosiveMissile missile = new EntityExplosiveMissile(world);

        // TODO convert this to some type of snapshot test, as in reality we are basically doing a manual version of that
        //     could use JSON to load java fields and types. Then match that to field/types on the loaded object.
        //      Needs to be versioned as well so we can do v1 vs v1.x+ saves

        // Validate we have a test file
        Assertions.assertNotNull(rpgSave);

        // Load entity custom save
        missile.readEntityFromNBT(rpgSave);

        // EntityExplosiveMissile
        final IExplosive explosive = missile.getCapability(ICBMClassicAPI.EXPLOSIVE_CAPABILITY, null);
        Assertions.assertEquals(ICBMClassicAPI.EXPLOSIVE_REGISTRY.getExplosiveData(10), explosive.getExplosiveData());

        // EntityMissile
        Assertions.assertTrue(missile.getMissileCapability().canRunFlightLogic());
        Assertions.assertNull(missile.getMissileCapability().getTargetData());
        Assertions.assertEquals(new DeadFlightLogic(135), missile.getMissileCapability().getFlightLogic());

        final EntityCause entitySourceData = new EntityCause();
        entitySourceData.setName("Player890");
        entitySourceData.setId(new UUID(2454671487114819752L, -8122821596986775482L));
        entitySourceData.setPlayer(true);
        Assertions.assertEquals(new ActionSource(world, new Vec3d(59.06195460480209, 75.15145375534576, 257.2760022607643), entitySourceData),
            missile.getMissileCapability().getMissileSource());

        // Projectile
        Assertions.assertEquals(new InGroundData(new BlockPos(10, 5, 290), EnumFacing.EAST, Blocks.STONE.getDefaultState()), missile.getInGroundData());
        Assertions.assertEquals(117, missile.ticksInAir);
        Assertions.assertEquals(45, missile.ticksInGround);

        // ICBM Entity
        assertFloating(43.1f, missile.getHealth(), 0.001f);

    }
}
