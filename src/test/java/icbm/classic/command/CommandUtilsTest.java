package icbm.classic.command;

import com.builtbroken.mc.testing.junit.TestManager;
import icbm.classic.content.entity.EntityExplosion;
import icbm.classic.content.entity.EntityExplosive;
import icbm.classic.content.entity.EntityFragments;
import icbm.classic.content.entity.EntityGrenade;
import icbm.classic.content.entity.missile.EntityMissile;
import lombok.SneakyThrows;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.passive.EntityBat;
import net.minecraft.entity.passive.EntitySheep;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

/**
 * Created by Robert Seifert on 1/2/20.
 */
public class CommandUtilsTest
{
    static TestManager testManager = new TestManager("CommandUtils");

    @AfterEach
    public void cleanupBetweenTests() {
        testManager.cleanupBetweenTests();
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
        final String[] input = new String[] {"tree"};
        final String[] output = CommandUtils.removeFront(input);
        Assertions.assertArrayEquals(new String[0], output);
    }

    @Test
    void removeFront_normalArray()
    {
        final String[] input = new String[] {"tree", "remove", "last"};
        final String[] output = CommandUtils.removeFront(input);
        Assertions.assertArrayEquals(new String[] {"remove", "last"}, output);
    }

    private static Stream<Arguments> provideIsICBMEntityData() {
        return Stream.of(
                Arguments.of(new EntityZombie(testManager.getWorld()), false),
                Arguments.of(new EntityBat(testManager.getWorld()), false),
                Arguments.of(new EntityEnderman(testManager.getWorld()), false),
                Arguments.of(new EntitySheep(testManager.getWorld()), false),
                Arguments.of(new EntityGrenade(testManager.getWorld()), true),
                Arguments.of(new EntityMissile(testManager.getWorld()), true),
                Arguments.of(new EntityExplosive(testManager.getWorld()), true),
                Arguments.of(new EntityFragments(testManager.getWorld()), true),
                Arguments.of(new EntityExplosion(testManager.getWorld()), true)
        );
    }


    @ParameterizedTest
    @MethodSource("provideIsICBMEntityData")
    void isICBMEntity_valid(Entity entity, boolean outcome) {
        Assertions.assertEquals(outcome, CommandUtils.isICBMEntity(entity));
    }

    private static Stream<Arguments> provideIsMissileData() {
        return Stream.of(
                Arguments.of(new EntityZombie(testManager.getWorld()), false),
                Arguments.of(new EntityBat(testManager.getWorld()), false),
                Arguments.of(new EntityEnderman(testManager.getWorld()), false),
                Arguments.of(new EntitySheep(testManager.getWorld()), false),
                Arguments.of(new EntityGrenade(testManager.getWorld()), false),
                Arguments.of(new EntityMissile(testManager.getWorld()), true),
                Arguments.of(new EntityExplosive(testManager.getWorld()), false),
                Arguments.of(new EntityFragments(testManager.getWorld()), false),
                Arguments.of(new EntityExplosion(testManager.getWorld()), false)
        );
    }


    @ParameterizedTest
    @MethodSource("provideIsMissileData")
    void isMissile_valid(Entity entity, boolean outcome) {
        Assertions.assertEquals(outcome, CommandUtils.isMissile(entity));
    }

    private static Stream<Arguments> provideParseRadiusGoodData() {
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

    private static Stream<Arguments> provideParseRadiusBadData() {
        return Stream.of(
                Arguments.of("1.0"),
                Arguments.of(".0"),
                Arguments.of("."),
                Arguments.of(","),
                Arguments.of("100,00"),
                Arguments.of(",.00"),
                Arguments.of("tree"),
                Arguments.of("3,tre")
        );
    }

    @ParameterizedTest
    @MethodSource("provideParseRadiusBadData")
    void parseRadius_badInput(String input)
    {
        Assertions.assertThrows(WrongUsageException.class, () -> CommandUtils.parseRadius(input));
    }
}
