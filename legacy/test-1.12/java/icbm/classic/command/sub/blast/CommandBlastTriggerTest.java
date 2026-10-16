package icbm.classic.command.sub.blast;

import com.builtbroken.mc.testing.junit.TestManager;
import com.builtbroken.mc.testing.junit.testers.DummyCommandSender;
import com.builtbroken.mc.testing.junit.testers.TestPlayer;
import icbm.classic.api.EnumTier;
import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.actions.status.IActionStatus;
import icbm.classic.api.reg.IExplosiveData;
import icbm.classic.command.FakeBlast;
import icbm.classic.command.ICBMCommands;
import icbm.classic.lib.actions.status.ActionResponses;
import icbm.classic.content.blast.BlastStatus;
import icbm.classic.lib.explosive.reg.ExplosiveRegistry;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.LinkedList;
import java.util.Queue;
import java.util.stream.Stream;

/**
 * Created by Robert Seifert on 1/6/20.
 */
public class CommandBlastTriggerTest
{

    private static TestManager testManager = new TestManager("CommandBlastTriggerTest", Assertions::fail);
    private final DummyCommandSender dummyCommandSender = new DummyCommandSender(testManager);

    private final CommandBlastTrigger command = new CommandBlastTrigger();
    private IExplosiveData fakeExData;
    private IActionStatus triggerState = ActionResponses.COMPLETED;

    private final Queue<FakeBlast> blastsCreated = new LinkedList();

    @BeforeEach
    public void setupBeforeTest()
    {
        ICBMClassicAPI.EXPLOSIVE_REGISTRY = new ExplosiveRegistry();
        fakeExData = ICBMClassicAPI.EXPLOSIVE_REGISTRY.register(new ResourceLocation("tree", "small"), EnumTier.ONE, (w, x, y, z, s) ->
        {
            FakeBlast fakeBlast = new FakeBlast(triggerState);
            blastsCreated.add(fakeBlast);
            return fakeBlast;
        });
    }

    @AfterEach
    public void cleanupBetweenTests()
    {
        ICBMClassicAPI.EXPLOSIVE_REGISTRY = null;
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
        Assertions.assertEquals("/trigger <id> <dim> <x> <y> <z> <scale>", dummyCommandSender.pollLastMessage());
    }

    @Test
    void help_player()
    {
        command.displayHelp(testManager.getPlayer());
        Assertions.assertEquals(2, testManager.getPlayer().messages.size());
        Assertions.assertEquals("/trigger <id> <dim> <x> <y> <z> <scale>", testManager.getPlayer().pollLastMessage());
        Assertions.assertEquals("/trigger <id> <scale>", testManager.getPlayer().pollLastMessage());
    }

    private static Stream<Arguments> provideBadCommandInputs()
    {
        return Stream.of(
                //Not enough args: player
                Arguments.of(new String[]{"tree:small"}, ICBMCommands.TRANSLATION_UNKNOWN_COMMAND, true),
                Arguments.of(new String[0], ICBMCommands.TRANSLATION_UNKNOWN_COMMAND, true),

                //Not enough args: server
                Arguments.of(new String[]{"tree:small", "0", "0", "0", "0"}, ICBMCommands.TRANSLATION_UNKNOWN_COMMAND, false),
                Arguments.of(new String[]{"tree:small", "0", "0", "0"}, ICBMCommands.TRANSLATION_UNKNOWN_COMMAND, false),
                Arguments.of(new String[]{"tree:small", "0", "0"}, ICBMCommands.TRANSLATION_UNKNOWN_COMMAND, false),
                Arguments.of(new String[]{"tree:small", "0"}, ICBMCommands.TRANSLATION_UNKNOWN_COMMAND, false),
                Arguments.of(new String[]{"tree:small"}, ICBMCommands.TRANSLATION_UNKNOWN_COMMAND, false),
                Arguments.of(new String[0], ICBMCommands.TRANSLATION_UNKNOWN_COMMAND, false),

                //Too many args
                Arguments.of(new String[]{"tree:small", "0", "0", "0", "0", "1", "s"}, ICBMCommands.TRANSLATION_UNKNOWN_COMMAND, false),

                //Bad blast ID
                Arguments.of(new String[]{"tree:big", "0", "0", "0", "0", "1"}, CommandBlastTrigger.TRANSLATION_ERROR_EXPLOSIVE_ID, false),
                Arguments.of(new String[]{"tree:big", "1"}, CommandBlastTrigger.TRANSLATION_ERROR_EXPLOSIVE_ID, false),

                //Bad scale
                Arguments.of(new String[]{"tree:small", "0", "0", "0", "0", "0"}, CommandBlastTrigger.TRANSLATION_ERROR_SCALE_ZERO, false),
                Arguments.of(new String[]{"tree:small", "0", "0", "0", "0", "-1"}, CommandBlastTrigger.TRANSLATION_ERROR_SCALE_ZERO, false),
                Arguments.of(new String[]{"tree:small", "0"}, CommandBlastTrigger.TRANSLATION_ERROR_SCALE_ZERO, true),
                Arguments.of(new String[]{"tree:small", "-1"}, CommandBlastTrigger.TRANSLATION_ERROR_SCALE_ZERO, true)
        );
    }

    @ParameterizedTest
    @MethodSource("provideBadCommandInputs")
    void command_badInput(String[] commandArgs, String errorMessage, boolean player)
    {
        final ICommandSender sender = player ? testManager.getPlayer() : testManager.getServer();

        //Validate we throw the right error
        final CommandException exception = Assertions.assertThrows(
                CommandException.class,
                () -> command.handleCommand(testManager.getServer(), sender, commandArgs)
        );

        //validate the error contains the right message
        Assertions.assertEquals(errorMessage, exception.getMessage());
    }

    private static Stream<Arguments> provideBlastOutputTypes()
    {
        return Stream.of(
                Arguments.of(ActionResponses.COMPLETED, CommandBlastTrigger.TRANSLATION_TRIGGERED),
                Arguments.of(BlastStatus.SETUP_ERROR, CommandBlastTrigger.TRANSLATION_ERROR_BLOCKED),
                Arguments.of(ActionResponses.UNKNOWN_ERROR, CommandBlastTrigger.TRANSLATION_ERROR)
        );
    }

    @ParameterizedTest
    @MethodSource("provideBlastOutputTypes")
    void command_short(IActionStatus stateToTest, String outputExpected) throws CommandException
    {
        this.triggerState = stateToTest;

        //Setup player for test
        final TestPlayer player = testManager.getPlayer();
        player.setPosition(100, 35, 200);

        //Trigger command
        final String[] commandArgs = new String[]{"tree:small", "2"};
        command.handleCommand(testManager.getServer(), player, commandArgs);

        //Should get 1 message back from the command
        Assertions.assertEquals(1, player.messages.size(), "Should have only received 1 chat message");
        Assertions.assertEquals(outputExpected, player.pollLastMessage(), "Chat message should match translation");

        validateBlastTrigger(player.world, player.getPosition().getX(), player.getPosition().getY(), player.getPosition().getZ(), 2, 1);
    }

    @ParameterizedTest
    @MethodSource("provideBlastOutputTypes")
    void command_long(IActionStatus stateToTest, String outputExpected) throws CommandException
    {
        this.triggerState = stateToTest;

        //Trigger command
        final String[] commandArgs = new String[]{"tree:small", "0", "100", "10", "20", "2"};
        command.handleCommand(testManager.getServer(), dummyCommandSender, commandArgs);

        //Should get 1 message back from the command
        Assertions.assertEquals(1, dummyCommandSender.messages.size(), "Should have only received 1 chat message");
        Assertions.assertEquals(outputExpected, dummyCommandSender.pollLastMessage(), "Chat message should match translation");

        validateBlastTrigger(testManager.getWorld(), 100, 10, 20, 2, 1);
    }

    @Test
    void command_long_player() throws CommandException
    {
        //Setup player for test
        final TestPlayer player = testManager.getPlayer();
        player.setPosition(100, 35, 200);

        //Trigger command
        final String[] commandArgs = new String[]{"tree:small", "~", "~", "~", "~", "2"};
        command.handleCommand(testManager.getServer(), player, commandArgs);

        //Should get 1 message back from the command
        Assertions.assertEquals(1, player.messages.size(), "Should have only received 1 chat message");
        Assertions.assertEquals(CommandBlastTrigger.TRANSLATION_TRIGGERED, player.pollLastMessage(), "Chat message should match translation");

        validateBlastTrigger(player.world, 100, 35, 200, 2, 1);
    }

    private void validateBlastTrigger(World world, int x, int y, int z, int size, int count)
    {
        //Should generate a single blast
        Assertions.assertEquals(count, blastsCreated.size(), "Expected the blast creation queue to contain " + count + " blast(s)");
        final FakeBlast blast = blastsCreated.poll();

        //Check position data
        Assertions.assertEquals(x, blast.xi(), "X position of the blast is incorrect");
        Assertions.assertEquals(y, blast.yi(), "Y position of the blast is incorrect");
        Assertions.assertEquals(z, blast.zi(), "Z position of the blast is incorrect");

        //Check world data
        Assertions.assertEquals(world, blast.world(), "World of the blast is incorrect");

        //Check explosive settings
        Assertions.assertEquals(fakeExData, blast.getActionData(), "Explosive data does not match");
        Assertions.assertNull(blast.customData, "Explosive custom data should be empty");
        Assertions.assertEquals(size, (int) Math.floor(blast.getBlastRadius()), "Explosive radius should be 2");
    }
}
