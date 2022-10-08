package icbm.classic.content.missile.entity.anti;

import com.builtbroken.mc.testing.junit.TestManager;
import icbm.classic.content.missile.entity.explosive.EntityExplosiveMissile;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntityShulker;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.world.World;
import org.junit.jupiter.api.*;

import java.util.function.Function;

public class RadarScanLogicTest {

    static TestManager testManager = new TestManager("radarScanLogic", Assertions::fail);

    final World world = testManager.getWorld();

    EntityAntiMissile antiMissile;

    @BeforeEach
    void beforeEach() {
        antiMissile = spawnEntity(EntityAntiMissile::new, 100, 100, 100);
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

    //Validate we can't target the host
    @DisplayName("No target: nothing nearby")
    @Test
    void noEntities_noTarget() {
       antiMissile.scanLogic.refreshTargets();
       Assertions.assertNull(antiMissile.scanLogic.getTarget());
    }

    //Validate we ignore common mobs TODO find a way to loop all vanilla mobs using an enum provider
    @DisplayName("No target: Vanilla mobs")
    @Test
    void vanillaMobs_noTarget() {

        final EntityZombie zombie = new EntityZombie(world);
        zombie.setPosition(100, 100, 104);
        world.spawnEntity(zombie);

        final EntityZombie zombie2 = new EntityZombie(world);
        zombie2.setPosition(102, 100, 104);
        world.spawnEntity(zombie2);

        final EntitySheep sheep = new EntitySheep(world);
        sheep.setPosition(101, 99, 101);
        world.spawnEntity(sheep);

        antiMissile.scanLogic.refreshTargets();
        Assertions.assertNull(antiMissile.scanLogic.getTarget());
    }

    //Validate we can't target dead missiles
    @DisplayName("No target: dead missiles")
    @Test
    void deadMissiles_noTarget() {
        spawnEntity(EntityExplosiveMissile::new, 100, 100, 105).setDead();

        antiMissile.scanLogic.refreshTargets();
        Assertions.assertNull(antiMissile.scanLogic.getTarget());
    }

    //Validate we stay within range of 30 meters
    @DisplayName("No target: outside range")
    @Test
    void outsideRange_noTarget() {
        spawnEntity(EntityExplosiveMissile::new, 100 + 31, 100, 100);
        spawnEntity(EntityExplosiveMissile::new, 100 - 31, 100, 100);
        spawnEntity(EntityExplosiveMissile::new, 100, 100, 100 + 31);
        spawnEntity(EntityExplosiveMissile::new, 100, 100, 100 - 31);
        spawnEntity(EntityExplosiveMissile::new, 100, 100 + 31, 100);
        spawnEntity(EntityExplosiveMissile::new, 100, 100 - 31, 100);

        antiMissile.scanLogic.refreshTargets();
        Assertions.assertNull(antiMissile.scanLogic.getTarget());
    }

    //Validate we target the only missile in existence
    @DisplayName("Target: single target")
    @Test
    void singleTarget_findOneTarget() {
        final EntityExplosiveMissile missile = spawnEntity(EntityExplosiveMissile::new, 105, 100, 100);

        antiMissile.scanLogic.refreshTargets();
        Assertions.assertSame(missile, antiMissile.scanLogic.getTarget());
    }

    //Validate we target the first few missiles only
    @DisplayName("Target: 10 targets but only store 5")
    @Test
    void severalTarget_findFiveTargets() {
        final EntityExplosiveMissile[] missiles = new EntityExplosiveMissile[]{
            spawnEntity(EntityExplosiveMissile::new, 101, 100, 100), // +1x
            spawnEntity(EntityExplosiveMissile::new, 100, 100, 102), // +2z
            spawnEntity(EntityExplosiveMissile::new, 100, 103, 100), // +3y
            spawnEntity(EntityExplosiveMissile::new, 100, 96, 100), // -4y
            spawnEntity(EntityExplosiveMissile::new, 100, 100, 95), //-5z
            spawnEntity(EntityExplosiveMissile::new, 94, 100, 100), //-6x
        };

        antiMissile.scanLogic.refreshTargets();

        //First target
        Assertions.assertSame(missiles[0], antiMissile.scanLogic.getTarget());
        missiles[0].setDead();

        //Second target
        Assertions.assertSame(missiles[1], antiMissile.scanLogic.getTarget());
        missiles[1].setDead();

        //Third target
        Assertions.assertSame(missiles[2], antiMissile.scanLogic.getTarget());
        missiles[2].setDead();

        //Fourth target
        Assertions.assertSame(missiles[3], antiMissile.scanLogic.getTarget());
        missiles[3].setDead();

        //Last target
        Assertions.assertSame(missiles[4], antiMissile.scanLogic.getTarget());
        missiles[4].setDead();

        //First target
        Assertions.assertNull(antiMissile.scanLogic.getTarget());
    }

    //Ensures that we can tick trigger an update
    @DisplayName("Validate lifecycle updates")
    @Test
    void tickingLifecycle() {
        final EntityExplosiveMissile missile = spawnEntity(EntityExplosiveMissile::new, 105, 100, 100);

        //After 10 ticks we should trigger scanning
        for(int i = 0; i < 10; i++) {
            antiMissile.scanLogic.tick();
            Assertions.assertNull(antiMissile.scanLogic.getTarget());
        }

        antiMissile.scanLogic.tick();
        Assertions.assertSame(missile, antiMissile.scanLogic.getTarget());

    }

    private <T extends Entity> T spawnEntity(Function<World, T> creator, double x, double y, double z) { //TODO move to testing helper library
        //Create entity and set position
        final Entity entity = creator.apply(world);
        entity.setPosition(x, y, z);

        //Force spawn so chunk loads
        entity.forceSpawn = true;

        //Validate we spawned correctly, fail test if we get wrong data as no entity can result in false positive
        Assertions.assertTrue(world.spawnEntity(entity), "Failed to spawn mob for test");

        return (T) entity;
    }
}
