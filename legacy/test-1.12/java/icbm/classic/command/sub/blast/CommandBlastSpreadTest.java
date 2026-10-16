package icbm.classic.command.sub.blast;

import com.builtbroken.mc.testing.junit.TestManager;
import com.builtbroken.mc.testing.junit.testers.DummyCommandSender;
import icbm.classic.api.EnumTier;
import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.reg.IExplosiveData;
import icbm.classic.command.FakeBlast;
import icbm.classic.command.ICBMCommands;
import icbm.classic.lib.actions.status.ActionResponses;
import icbm.classic.lib.explosive.reg.ExplosiveRegistry;
import net.minecraft.command.CommandException;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Created by Robert Seifert on 1/6/20.
 */
public class CommandBlastSpreadTest
{
    private static TestManager testManager = new TestManager("CommandBlastSpreadTest", Assertions::fail);
    private final DummyCommandSender dummyCommandSender = new DummyCommandSender(testManager);

    private final CommandBlastSpread command = new CommandBlastSpread();

    private IExplosiveData fakeExData;

    private final Queue<FakeBlast> blastsCreated = new LinkedList();

    @BeforeEach
    public void setupBeforeTest()
    {
        ICBMClassicAPI.EXPLOSIVE_REGISTRY = new ExplosiveRegistry();
        fakeExData = ICBMClassicAPI.EXPLOSIVE_REGISTRY.register(new ResourceLocation("tree", "small"), EnumTier.ONE, (w, x, y, z, s) ->
        {
            FakeBlast fakeBlast = (FakeBlast) new FakeBlast(ActionResponses.COMPLETED).setBlastWorld(w).setBlastPosition(x, y, z);
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
        Assertions.assertEquals("/spread <count> <distance> <id> <dim> <x> <y> <z> <scale>", dummyCommandSender.pollLastMessage());
    }

    @Test
    void help_player()
    {
        command.displayHelp(testManager.getPlayer());
        Assertions.assertEquals(1, testManager.getPlayer().messages.size());
        Assertions.assertEquals("/spread <count> <distance> <id> <dim> <x> <y> <z> <scale>", testManager.getPlayer().pollLastMessage());
    }

    private static Stream<Arguments> provideBadCommandInputs()
    {

        final List<Arguments> commands = new LinkedList();

        //Command inputs under 8 values
        List<String> strings = new ArrayList();
        for (int i = 0; i < 8; i++)
        {
            commands.add(Arguments.of(strings.toArray(new String[strings.size()]), ICBMCommands.TRANSLATION_UNKNOWN_COMMAND));
            strings.add("0");
        }

        //Command inputs over 8
        for (int i = 0; i < 3; i++)
        {
            strings.add("0");
            commands.add(Arguments.of(strings.toArray(new String[strings.size()]), ICBMCommands.TRANSLATION_UNKNOWN_COMMAND));
        }

        //Bad blast ID
        commands.add(Arguments.of(new String[]{"1", "1", "tree:big", "0", "0", "0", "0", "1"}, CommandBlastTrigger.TRANSLATION_ERROR_EXPLOSIVE_ID));

        //Bad scale
        commands.add(Arguments.of(new String[]{"1", "1", "tree:small", "0", "0", "0", "0", "0"}, CommandBlastTrigger.TRANSLATION_ERROR_SCALE_ZERO));
        commands.add(Arguments.of(new String[]{"1", "1", "tree:small", "0", "0", "0", "0", "-1"}, CommandBlastTrigger.TRANSLATION_ERROR_SCALE_ZERO));

        //TODO small count & distance

        return commands.stream();
    }

    @ParameterizedTest
    @MethodSource("provideBadCommandInputs")
    void command_badInput(String[] commandArgs, String errorMessage)
    {
        //Validate we throw the right error
        final CommandException exception = Assertions.assertThrows(
                CommandException.class,
                () -> command.handleCommand(testManager.getServer(), testManager.getServer(), commandArgs)
        );

        //validate the error contains the right message
        Assertions.assertEquals(errorMessage, exception.getMessage());
    }

    private static Stream<Arguments> provideGoodCommandInputs()
    {
        return Stream.of(
                Arguments.of(new String[]{"1", "10", "tree:small", "0", "123", "67", "345", "2"}, 123, 67, 345, 10),
                Arguments.of(new String[]{"1", "100", "tree:small", "0", "123", "67", "345", "2"}, 123, 67, 345, 100),
                Arguments.of(new String[]{"1", "10", "tree:small", "~", "~", "~", "~", "2"}, 123, 67, 345, 10)
        );
    }

    @ParameterizedTest
    @MethodSource("provideGoodCommandInputs")
    void checkCommand(String[] args, int x, int y, int z, int distance) throws CommandException
    {
        dummyCommandSender.position = new Vec3d(x, y, z);
        command.handleCommand(testManager.getServer(), dummyCommandSender, args);

        //Validate message
        Assertions.assertEquals(1, dummyCommandSender.messages.size());
        Assertions.assertEquals(CommandBlastSpread.TRANSLATION_SPREAD_START, dummyCommandSender.pollLastMessage());

        //We should spawn 9 blasts
        Assertions.assertEquals(9, blastsCreated.size());

        final Set<BlockPos> positions = new HashSet();
        positions.add(new BlockPos(x, y, z));
        positions.add(new BlockPos(x + distance, y, z + distance));
        positions.add(new BlockPos(x - distance, y, z - distance));
        positions.add(new BlockPos(x - distance, y, z + distance));
        positions.add(new BlockPos(x + distance, y, z - distance));
        positions.add(new BlockPos(x + distance, y, z));
        positions.add(new BlockPos(x - distance, y, z));
        positions.add(new BlockPos(x, y, z + distance));
        positions.add(new BlockPos(x, y, z - distance));

        //Validate blast contents
        do
        {
            final FakeBlast blast = blastsCreated.poll();
            final BlockPos pos = blast.getPos();

            //Check that we match position, then remove to catch duplicate spawns
            Assertions.assertTrue(positions.contains(pos));
            positions.remove(pos);

            //Check world data
            Assertions.assertEquals(testManager.getWorld(), blast.world(), "World of the blast is incorrect");


            //Check explosive settings
            Assertions.assertEquals(fakeExData, blast.getActionData(), "Explosive data does not match");
            Assertions.assertNull(blast.customData, "Explosive custom data should be empty");
            Assertions.assertEquals(2, (int) Math.floor(blast.getBlastRadius()), "Explosive radius should be 2");

        } while (!blastsCreated.isEmpty());
    }
}
