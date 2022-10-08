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

    private <T extends Entity> T spawnEntity(Function<World, T> creator, double x, double y, double z) {
        final Entity entity = creator.apply(world);
        entity.setPosition(x, y, z);
        entity.forceSpawn = true;
        Assertions.assertTrue(world.spawnEntity(entity), "Failed to spawn mob for test");
        return (T) entity;
    }
}
