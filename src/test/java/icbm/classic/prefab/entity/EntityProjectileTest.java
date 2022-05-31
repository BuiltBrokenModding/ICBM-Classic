package icbm.classic.prefab.entity;

import com.builtbroken.mc.testing.junit.TestManager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.Collections;

public class EntityProjectileTest
{
    public static final String[] expectedNbtKeys = new String[]{
        "ground",
        "flags",
        "ticks",
        "source"
    };

    static TestManager testManager = new TestManager("projectile", Assertions::fail);

    final World world = testManager.getWorld();

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
    class SaveLoad {

        @Test
        void readEntityFromNBT_empty() {
            final EntityProjectile projectile = new EntityProjectile(world);

            //Invoke load with empty data
            final NBTTagCompound saveToLoad = new NBTTagCompound();
            projectile.readEntityFromNBT(saveToLoad);

            //Test is to check that we don't crash while loading
        }

        @Test
        void writeEntityToNBT_rootCheck() {
            //Setup save target
            final EntityProjectile projectile = new EntityProjectile(world);

            //Invoke save
            final NBTTagCompound save = new NBTTagCompound();
            projectile.writeEntityToNBT(save);

            //Test we have our root fields, objects themselves will test exact saving
            final ArrayList<String> keys = new ArrayList<>();
            Collections.addAll(keys, EntityICBMTest.expectedNbtKeys);
            Collections.addAll(keys, expectedNbtKeys);
            keys.sort(String::compareTo);

            final ArrayList<String> saveKeys = new ArrayList<>(save.getKeySet());
            saveKeys.sort(String::compareTo);
            Assertions.assertEquals(keys, saveKeys);
        }


        void fullSaveLoad() {

        }
    }
}