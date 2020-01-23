package icbm.classic.command.sub;

import com.builtbroken.mc.testing.junit.TestManager;
import com.builtbroken.mc.testing.junit.testers.DummyCommandSender;
import icbm.classic.TestUtils;
import icbm.classic.command.ICBMCommands;
import icbm.classic.content.entity.missile.EntityMissile;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.util.math.Vec3d;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

/**
 * Created by Robert Seifert on 1/14/20.
 */
public class CommandRemoveTest
{
    private static TestManager testManager = new TestManager("CommandRemoveTest", Assertions::fail);
    private final DummyCommandSender dummyCommandSender = new DummyCommandSender(testManager);

    private final CommandRemove command = new CommandRemove();

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
    void help_server()
    {
        command.displayHelp(dummyCommandSender);
        Assertions.assertEquals(1, dummyCommandSender.messages.size());
        Assertions.assertEquals("/remove <all/missiles/explosions> <dim> <x> <y> <z> <radius>", dummyCommandSender.messages.poll().getUnformattedText());
    }

    @Test
    void help_player()
    {
        command.displayHelp(testManager.getPlayer());
        Assertions.assertEquals(2, testManager.getPlayer().messages.size());
        Assertions.assertEquals("/remove <all/missiles/explosions> <dim> <x> <y> <z> <radius>", testManager.getPlayer().messages.poll().getUnformattedText());
        Assertions.assertEquals("/remove <all/missiles/explosions> [radius]", testManager.getPlayer().messages.poll().getUnformattedText());
    }


    @Test
    void command_zeroArgs()
    {
        final WrongUsageException exception = Assertions.assertThrows(WrongUsageException.class,
                () -> command.handleCommand(testManager.getServer(), dummyCommandSender, new String[0]));
        Assertions.assertEquals(ICBMCommands.TRANSLATION_UNKNOWN_COMMAND, exception.getMessage());
    }

    private static Stream<Arguments> provideArgsToTest()
    {
        return Stream.of(
                Arguments.of(new String[]{"all"}, true),
                Arguments.of(new String[]{"missiles"}, true),
                Arguments.of(new String[]{"explosions"}, false),

                Arguments.of(new String[]{"all", "-1"}, true),
                Arguments.of(new String[]{"missiles", "-1"}, true),
                Arguments.of(new String[]{"explosions", "-1"}, false),

                Arguments.of(new String[]{"all", "100"}, true),
                Arguments.of(new String[]{"missiles", "100"}, true),
                Arguments.of(new String[]{"explosions", "100"}, false),

                Arguments.of(new String[]{"all", "0", "10", "20", "30", "100"}, true),
                Arguments.of(new String[]{"missiles", "0", "10", "20", "30", "100"}, true),
                Arguments.of(new String[]{"explosions", "0", "10", "20", "30", "100"}, false),

                Arguments.of(new String[]{"all", "~", "~", "~", "~", "100"}, true),
                Arguments.of(new String[]{"missiles", "~", "~", "~", "~", "100"}, true),
                Arguments.of(new String[]{"explosions", "~", "~", "~", "~", "100"}, false)
        );
    }

    @ParameterizedTest
    @MethodSource("provideArgsToTest")
    void command_removeNothing(String[] args, boolean removeMissile)
    {
        dummyCommandSender.position = new Vec3d(100, 200, 100);

        //Spawn some sheep to act as decoys
        TestUtils.sheep(testManager.getWorld(), 100, 20, 100);
        TestUtils.sheep(testManager.getWorld(), 100, 30, 100);
        TestUtils.sheep(testManager.getWorld(), 100, 40, 100);
        Assertions.assertEquals(3, testManager.getWorld().loadedEntityList.size(), "Should start with 3 sheep");

        //Trigger command
        Assertions.assertDoesNotThrow(() -> command.handleCommand(testManager.getServer(), dummyCommandSender, args));

        //Validate output
        Assertions.assertEquals(1, dummyCommandSender.messages.size(), "Should have 1 chat message");
        Assertions.assertEquals(CommandRemove.TRANSLATION_REMOVE, dummyCommandSender.pollLastMessage(), "Should get translation");

        //Should still have 3 sheep
        Assertions.assertEquals(3, testManager.getWorld().loadedEntityList.size(), "Should end with 3 sheep");
    }

    @ParameterizedTest
    @MethodSource("provideArgsToTest")
    void command_removeMissiles(String[] args, boolean removeMissile)
    {
        dummyCommandSender.position = new Vec3d(100, 20, 100);

        //Spawn some sheep to act as decoys
        TestUtils.sheep(testManager.getWorld(), 100, 20, 100);
        TestUtils.sheep(testManager.getWorld(), 100, 30, 100);
        TestUtils.sheep(testManager.getWorld(), 100, 40, 100);

        TestUtils.missile(testManager.getWorld(), 100, 10, 100);
        TestUtils.missile(testManager.getWorld(), 100, 20, 100);

        //Validate start condition
        Assertions.assertEquals(3, testManager.getWorld().loadedEntityList.stream().filter(e -> e instanceof EntitySheep).count(), "Should start with 3 sheep");
        Assertions.assertEquals(2, testManager.getWorld().loadedEntityList.stream().filter(e -> e instanceof EntityMissile).count(), "Should start with 2 missiles");

        //Trigger command
        Assertions.assertDoesNotThrow(() -> command.handleCommand(testManager.getServer(), dummyCommandSender, args));

        //Validate output
        Assertions.assertEquals(1, dummyCommandSender.messages.size(), "Should have 1 chat message");
        Assertions.assertEquals(CommandRemove.TRANSLATION_REMOVE, dummyCommandSender.pollLastMessage(), "Should get translation");

        //Should still have 3 sheep
        Assertions.assertEquals(3, testManager.getWorld().loadedEntityList.stream().filter(e -> e instanceof EntitySheep).count(), "Should end with 3 sheep");
        Assertions.assertEquals(removeMissile ? 0 : 2, testManager.getWorld().loadedEntityList.stream().filter(e -> e instanceof EntityMissile).filter(Entity::isEntityAlive).count(), "Should end with 0 missiles");
    }
}
