package icbm.classic.content.missile.entity.explosive;

import com.builtbroken.mc.testing.junit.TestManager;
import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.content.missile.logic.flight.DeadFlightLogic;
import icbm.classic.content.missile.logic.reg.MissileFlightLogicRegistry;
import icbm.classic.content.missile.targeting.BasicTargetData;
import icbm.classic.content.missile.targeting.reg.MissileTargetRegistry;
import icbm.classic.prefab.entity.EntityICBMTest;
import icbm.classic.prefab.entity.EntityProjectileTest;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.Collections;

public class EntityExplosiveMissileTest
{
    public static final String[] expectedNbtKeys = new String[]{
        "missile",
        "explosive"
    };
    static TestManager testManager = new TestManager("missile", Assertions::fail);

    final World world = testManager.getWorld();

    @BeforeAll
    public static void beforeAllTests()
    {
        //Setup target registry
        final MissileTargetRegistry targetReg = new MissileTargetRegistry();
        ICBMClassicAPI.MISSILE_TARGET_DATA_REGISTRY = targetReg;
        targetReg.register(BasicTargetData.REG_NAME, BasicTargetData::new);

        //Setup flight registry
        final MissileFlightLogicRegistry flightReg = new MissileFlightLogicRegistry();
        ICBMClassicAPI.MISSILE_FLIGHT_LOGIC_REGISTRY = flightReg;
        flightReg.register(DeadFlightLogic.REG_NAME, DeadFlightLogic::new);
    }

    @AfterAll
    public static void afterAllTests()
    {
        testManager.tearDownTest();
    }



    @AfterEach
    public void afterEachTest()
    {
        testManager.cleanupBetweenTests();
    }

    @Nested
    class SaveLoad
    {

        @Test
        @DisplayName("Validate we load from empty state")
        void readEntityFromNBT_empty()
        {
            final EntityExplosiveMissile missile = new EntityExplosiveMissile(world);

            //Invoke load with empty data
            final NBTTagCompound saveToLoad = new NBTTagCompound();
            missile.readEntityFromNBT(saveToLoad);

            //Test is to check that we don't crash while loading
        }

        @Test
        @DisplayName("Validate we save root keys")
        void writeEntityToNBT_rootCheck()
        {
            //Setup save target
            final EntityExplosiveMissile missile = new EntityExplosiveMissile(world);
            missile.getMissileCapability().setTargetData(new BasicTargetData(1, 2, 3));
            missile.getMissileCapability().setFlightLogic(new DeadFlightLogic(4567));
            missile.explosive.setStack(new ItemStack(Items.STONE_AXE));

            //Invoke save
            final NBTTagCompound save = new NBTTagCompound();
            missile.writeEntityToNBT(save);

            //Generate root list
            //  If fields change make sure to update full save/load test
            final ArrayList<String> keys = new ArrayList<>();
            Collections.addAll(keys, EntityICBMTest.expectedNbtKeys);
            Collections.addAll(keys, EntityProjectileTest.expectedNbtKeys);
            Collections.addAll(keys, expectedNbtKeys);
            keys.sort(String::compareTo);

            //Test we have our root fields, objects themselves will test exact saving
            final ArrayList<String> saveKeys = new ArrayList<>(save.getKeySet());
            saveKeys.sort(String::compareTo);
            Assertions.assertEquals(keys, saveKeys);
        }

        @Test
        @DisplayName("Validate we can save then load needed data")
        void fullSaveLoad()
        {
            //Setup save target
            final EntityExplosiveMissile missile = new EntityExplosiveMissile(world);
            missile.getMissileCapability().setTargetData(new BasicTargetData(1, 2, 3));
            missile.getMissileCapability().setFlightLogic(new DeadFlightLogic(4567));
            missile.explosive.setStack(new ItemStack(Items.STONE_AXE));

            //Invoke save
            final NBTTagCompound save = new NBTTagCompound();
            missile.writeEntityToNBT(save);

            //Setup load target
            final EntityExplosiveMissile newMissile = new EntityExplosiveMissile(world);

            //Invoke load
            newMissile.readEntityFromNBT(save);

            //Validate fields from base entity
            Assertions.assertEquals(missile.getHealth(), newMissile.getHealth());

            //Validate fields from projectile entity
            Assertions.assertEquals(missile.tilePos, newMissile.tilePos);
            Assertions.assertEquals(missile.sideTile, newMissile.sideTile);
            Assertions.assertEquals(missile.blockInside, newMissile.blockInside);
            Assertions.assertEquals(missile.inGround, newMissile.inGround);
            Assertions.assertEquals(missile.ticksInAir, newMissile.ticksInAir);
            Assertions.assertEquals(missile.ticksInGround, missile.ticksInGround);
            Assertions.assertEquals(missile.sourceOfProjectile, newMissile.sourceOfProjectile);
            Assertions.assertEquals(missile.shootingEntityUUID, newMissile.shootingEntityUUID);

            //Validate fields from missile
            Assertions.assertEquals(missile.explosive, newMissile.explosive);
            Assertions.assertEquals(missile.getMissileCapability(), newMissile.getMissileCapability());
        }
    }

}