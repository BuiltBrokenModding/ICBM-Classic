package icbm.classic.command.sub.blast;

import com.builtbroken.mc.testing.junit.TestManager;
import com.builtbroken.mc.testing.junit.testers.DummyCommandSender;
import com.builtbroken.mc.testing.junit.testers.TestPlayer;
import icbm.classic.api.EnumTier;
import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.explosion.BlastState;
import icbm.classic.api.reg.IExplosiveData;
import icbm.classic.command.FakeBlast;
import icbm.classic.lib.explosive.reg.ExplosiveRegistry;
import net.minecraft.command.CommandException;
import net.minecraft.command.WrongUsageException;
import net.minecraft.util.ResourceLocation;
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
    private BlastState triggerState = BlastState.TRIGGERED;

    private final Queue<FakeBlast> blastsCreated = new LinkedList();

    @BeforeEach
    public void setupBeforeTest()
    {
        ICBMClassicAPI.EXPLOSIVE_REGISTRY = new ExplosiveRegistry();
        fakeExData = ICBMClassicAPI.EXPLOSIVE_REGISTRY.register(new ResourceLocation("tree", "small"), EnumTier.ONE, () ->
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

    @Test
    void command_long_badBlastID()
    {
        final String[] commandArgs = new String[]{"tree:big", "0", "0", "0", "0", "1"};
        Assertions.assertThrows(WrongUsageException.class,
                () -> command.handleCommand(testManager.getServer(), testManager.getServer(), commandArgs));
    }

    @Test
    void command_short_badBlastID()
    {
        final String[] commandArgs = new String[]{"tree:big", "1"};
        Assertions.assertThrows(WrongUsageException.class,
                () -> command.handleCommand(testManager.getServer(), testManager.getPlayer(), commandArgs));
    }

    private static Stream<Arguments> provideCommandShortData()
    {
        return Stream.of(
                Arguments.of(BlastState.TRIGGERED, CommandBlastTrigger.TRANSLATION_TRIGGERED),
                Arguments.of(BlastState.THREADING, CommandBlastTrigger.TRANSLATION_THREADING),
                Arguments.of(BlastState.FORGE_EVENT_CANCEL, CommandBlastTrigger.TRANSLATION_ERROR_BLOCKED),
                Arguments.of(BlastState.NULL, CommandBlastTrigger.TRANSLATION_ERROR_NULL),
                Arguments.of(BlastState.ERROR, CommandBlastTrigger.TRANSLATION_ERROR),
                Arguments.of(BlastState.ALREADY_TRIGGERED, CommandBlastTrigger.TRANSLATION_ERROR_TRIGGERED)
        );
    }

    @ParameterizedTest
    @MethodSource("provideCommandShortData")
    void command_short(BlastState stateToTest, String outputExpected) throws CommandException
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

        //Should generate a single blast
        Assertions.assertEquals(1, blastsCreated.size());


        final FakeBlast blast = blastsCreated.poll();

        //Check position data
        Assertions.assertEquals(100, blast.xi(), "X position of the blast is incorrect");
        Assertions.assertEquals(35, blast.yi(), "Y position of the blast is incorrect");
        Assertions.assertEquals(200, blast.zi(), "Z position of the blast is incorrect");

        //Check world data
        Assertions.assertEquals(player.world, blast.world(), "World of the blast is incorrect");

        //Check explosive settings
        Assertions.assertEquals(fakeExData, blast.getExplosiveData(), "Explosive data does not match");
        Assertions.assertNull(blast.customData, "Explosive custom data should be empty");
        Assertions.assertEquals(2, (int)Math.floor(blast.getBlastRadius()), "Explosive radius should be 2");
    }
}
