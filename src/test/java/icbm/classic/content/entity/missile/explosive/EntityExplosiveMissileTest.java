package icbm.classic.content.entity.missile.explosive;

import com.builtbroken.mc.testing.junit.TestManager;
import icbm.classic.TestUtils;
import icbm.classic.content.entity.missile.logic.flight.DeadFlightLogic;
import icbm.classic.content.entity.missile.targeting.BasicTargetData;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class EntityExplosiveMissileTest
{
    static TestManager testManager = new TestManager("missile", Assertions::fail);

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

    @Test
    void readEntityFromNBT() {
        final EntityExplosiveMissile missile = new EntityExplosiveMissile(world);
    }

    @Test
    void writeEntityToNBT() {
        //Setup save target
        final EntityExplosiveMissile missile = new EntityExplosiveMissile(world);
        missile.missileCapability.setTargetData(new BasicTargetData(1, 2, 3));
        missile.setFlightLogic(new DeadFlightLogic(4567));
        missile.explosiveID = 4567;
        missile.blastData = new NBTTagCompound();
        missile.blastData.setInteger("scale", 4);

        //Setup save we expect to get
        final NBTTagCompound expectedSave = new NBTTagCompound();

        expectedSave.setInteger("explosiveID", 4567);
        final NBTTagCompound blastData = new NBTTagCompound();
        blastData.setInteger("scale", 4);
        expectedSave.setTag("additionalMissileData", blastData);

        final NBTTagCompound components = new NBTTagCompound();
        expectedSave.setTag("components", components);

        final NBTTagCompound missileCap = new NBTTagCompound();
        missileCap.setBoolean("do_flight", false);
        final NBTTagCompound target = new NBTTagCompound();
        missileCap.setTag("target", target);
        components.setTag("missile", missileCap);

        final NBTTagCompound flightCap = new NBTTagCompound();
        flightCap.setString("id", "icbmclassic:dead");
        final NBTTagCompound flightData = new NBTTagCompound();
        flightData.setInteger("fuel", 4567);
        flightCap.setTag("data", flightData);
        components.setTag("flight", flightCap);

        //Invoke save
        final NBTTagCompound save = new NBTTagCompound();
        missile.writeEntityToNBT(save);

        TestUtils.assertContains(expectedSave, save);
    }
}
