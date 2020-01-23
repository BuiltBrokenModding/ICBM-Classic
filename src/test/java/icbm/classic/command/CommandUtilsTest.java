package icbm.classic.command;

import com.builtbroken.mc.testing.junit.TestManager;
import com.builtbroken.mc.testing.junit.testers.DummyCommandSender;
import com.builtbroken.mc.testing.junit.world.FakeWorld;
import icbm.classic.content.entity.EntityExplosion;
import icbm.classic.content.entity.EntityExplosive;
import icbm.classic.content.entity.EntityFlyingBlock;
import icbm.classic.content.entity.EntityFragments;
import icbm.classic.content.entity.EntityGrenade;
import icbm.classic.content.entity.missile.EntityMissile;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.passive.EntityBat;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.util.EntitySelectors;
import net.minecraft.world.World;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

/**
 * Created by Robert Seifert on 1/2/20.
 */
public class CommandUtilsTest
{

    private static TestManager testManager = new TestManager("CommandUtils", Assertions::fail);

    @AfterEach
    public void cleanupBetweenTests()
    {
        testManager.cleanupBetweenTests();
    }

    @AfterAll
    public static void tearDown()
    {
        testManager.tearDownTest();
    }

    @Test
    void removeFront_zeroLength()
    {
        final String[] input = new String[0];
        final String[] output = CommandUtils.removeFront(input);
        Assertions.assertArrayEquals(new String[0], output);
    }

    @Test
    void removeFront_singleLength()
    {
        final String[] input = new String[]{"tree"};
        final String[] output = CommandUtils.removeFront(input);
        Assertions.assertArrayEquals(new String[0], output);
    }

    @Test
    void removeFront_normalArray()
    {
        final String[] input = new String[]{"tree", "remove", "last"};
        final String[] output = CommandUtils.removeFront(input);
        Assertions.assertArrayEquals(new String[]{"remove", "last"}, output);
    }

    private static Stream<Arguments> provideIsICBMEntityData()
    {
        final FakeWorld world = FakeWorld.newWorld("isICBMEntity");
        return Stream.of(
                Arguments.of(new EntityZombie(world), false),
                Arguments.of(new EntityBat(world), false),
                Arguments.of(new EntityEnderman(world), false),
                Arguments.of(new EntitySheep(world), false),
                Arguments.of(new EntityGrenade(world), true),
                Arguments.of(new EntityMissile(world), true),
                Arguments.of(new EntityExplosive(world), true),
                Arguments.of(new EntityFragments(world), true),
                Arguments.of(new EntityExplosion(world), true),
                Arguments.of(new EntityFlyingBlock(world), true)
        );
    }


    @ParameterizedTest
    @MethodSource("provideIsICBMEntityData")
    void isICBMEntity_valid(Entity entity, boolean outcome)
    {
        Assertions.assertEquals(outcome, CommandUtils.isICBMEntity(entity));
    }

    private static Stream<Arguments> provideIsMissileData()
    {
        final FakeWorld world = FakeWorld.newWorld("isMissile");
        return Stream.of(
                Arguments.of(new EntityZombie(world), false),
                Arguments.of(new EntityBat(world), false),
                Arguments.of(new EntityEnderman(world), false),
                Arguments.of(new EntitySheep(world), false),
                Arguments.of(new EntityGrenade(world), false),
                Arguments.of(new EntityMissile(world), true),
                Arguments.of(new EntityExplosive(world), false),
                Arguments.of(new EntityFragments(world), false),
                Arguments.of(new EntityExplosion(world), false),
                Arguments.of(new EntityFlyingBlock(world), false)
        );
    }


    @ParameterizedTest
    @MethodSource("provideIsMissileData")
    void isMissile_valid(Entity entity, boolean outcome)
    {
        Assertions.assertEquals(outcome, CommandUtils.isMissile(entity));
    }

    private static Stream<Arguments> provideParseRadiusGoodData()
    {
        return Stream.of(
                Arguments.of("1", 1),
                Arguments.of("22", 22),
                Arguments.of("987", 987),
                Arguments.of("1234567", 1234567)
        );
    }

    @ParameterizedTest
    @MethodSource("provideParseRadiusGoodData")
    void parseRadius_goodInput(String input, int output) throws WrongUsageException
    {
        Assertions.assertEquals(output, CommandUtils.parseRadius(input));
    }

    private static Stream<Arguments> provideParseRadiusBadData()
    {
        return Stream.of(
                Arguments.of("1.0"),
                Arguments.of(".0"),
                Arguments.of("."),
                Arguments.of(","),
                Arguments.of("100,00"),
                Arguments.of(",.00"),
                Arguments.of("tree"),
                Arguments.of("3,tre"),
                Arguments.of("-1"),
                Arguments.of("-100"),
                Arguments.of("-")
        );
    }

    @ParameterizedTest
    @MethodSource("provideParseRadiusBadData")
    void parseRadius_badInput(String input)
    {
        Assertions.assertThrows(WrongUsageException.class, () -> CommandUtils.parseRadius(input));
    }

    @Test
    void getEntities_withRange_found()
    {
        final World world = testManager.getWorld();

        //Sheep in range
        EntitySheep sheep = new EntitySheep(world);
        sheep.forceSpawn = true;
        sheep.setPosition(100, 10, 100);
        world.spawnEntity(sheep);

        sheep = new EntitySheep(world);
        sheep.forceSpawn = true;
        sheep.setPosition(100, 11, 100);
        world.spawnEntity(sheep);

        sheep = new EntitySheep(world);
        sheep.forceSpawn = true;
        sheep.setPosition(100, 12, 100);
        world.spawnEntity(sheep);

        //Sheep not in range
        sheep = new EntitySheep(world);
        sheep.forceSpawn = true;
        sheep.setPosition(200, 12, 100);
        world.spawnEntity(sheep);

        sheep = new EntitySheep(world);
        sheep.forceSpawn = true;
        sheep.setPosition(100, 12, 300);
        world.spawnEntity(sheep);

        sheep = new EntitySheep(world);
        sheep.forceSpawn = true;
        sheep.setPosition(100, 12, 500);
        world.spawnEntity(sheep);

        //Should find 3 sheep
        List<Entity> list = CommandUtils.getEntities(world, 100, 11, 100, 5, EntitySelectors.NOT_SPECTATING::test);
        Assertions.assertEquals(3, list.size());
    }

    @Test
    void getEntities_allEntities_found()
    {
        final World world = testManager.getWorld();

        //Sheep in range
        EntitySheep sheep = new EntitySheep(world);
        sheep.forceSpawn = true;
        sheep.setPosition(100, 10, 100);
        world.spawnEntity(sheep);

        sheep = new EntitySheep(world);
        sheep.forceSpawn = true;
        sheep.setPosition(100, 11, 100);
        world.spawnEntity(sheep);

        sheep = new EntitySheep(world);
        sheep.forceSpawn = true;
        sheep.setPosition(100, 12, 100);
        world.spawnEntity(sheep);

        //Sheep not in range

        sheep = new EntitySheep(world);
        sheep.forceSpawn = true;
        sheep.setPosition(200, 12, 100);
        world.spawnEntity(sheep);

        sheep = new EntitySheep(world);
        sheep.forceSpawn = true;
        sheep.setPosition(100, 12, 300);
        world.spawnEntity(sheep);

        sheep = new EntitySheep(world);
        sheep.forceSpawn = true;
        sheep.setPosition(100, 12, 500);
        world.spawnEntity(sheep);

        //Should find 3 sheep
        List<Entity> list = CommandUtils.getEntities(world, 0, 0, 0, -1, EntitySelectors.NOT_SPECTATING::test);
        Assertions.assertEquals(6, list.size());
    }

    @Test
    void getEntities_withRange_nothing()
    {
        final World world = testManager.getWorld();

        List<Entity> list = CommandUtils.getEntities(world, 100, 11, 100, 5, EntitySelectors.NOT_SPECTATING::test);
        Assertions.assertEquals(0, list.size());
    }

    @Test
    void getEntities_allEntities_nothing()
    {
        final World world = testManager.getWorld();

        //Should find 3 sheep
        List<Entity> list = CommandUtils.getEntities(world, 0, 0, 0, -1, EntitySelectors.NOT_SPECTATING::test);
        Assertions.assertEquals(0, list.size());
    }


    @Test
    void getNumber_playerTilde_zero() throws WrongUsageException
    {
        final DummyCommandSender dummyCommandSender = new DummyCommandSender(testManager);
        double result = CommandUtils.getNumber(dummyCommandSender, "~", 100);
        Assertions.assertEquals(100.0, result);
    }

    @Test
    void getNumber_playerTilde_offset() throws WrongUsageException
    {
        final DummyCommandSender dummyCommandSender = new DummyCommandSender(testManager);
        double result = CommandUtils.getNumber(dummyCommandSender, "~3", 100);
        Assertions.assertEquals(103.0, result);
    }

    @Test
    void getNumber_server() throws WrongUsageException
    {
        double result = CommandUtils.getNumber(testManager.getServer(), "3", 100);
        Assertions.assertEquals(3.0, result);
    }

    @Test
    void getNumber_serverTilde_zero()
    {
        Assertions.assertThrows(WrongUsageException.class, () -> CommandUtils.getNumber(testManager.getServer(), "~", 100));
    }

    @Test
    void getNumber_serverTilde_offset()
    {
        Assertions.assertThrows(WrongUsageException.class, () -> CommandUtils.getNumber(testManager.getServer(), "~3", 100));
    }
}
