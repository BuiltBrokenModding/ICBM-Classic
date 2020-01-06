package icbm.classic.command.sub.blast;

import com.builtbroken.mc.testing.junit.TestManager;
import com.builtbroken.mc.testing.junit.testers.DummyCommandSender;
import icbm.classic.api.EnumTier;
import icbm.classic.api.ICBMClassicAPI;
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

import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by Robert Seifert on 1/6/20.
 */
public class CommandBlastTriggerTest
{

    private static TestManager testManager = new TestManager("CommandBlastTriggerTest", Assertions::fail);
    private final DummyCommandSender dummyCommandSender = new DummyCommandSender(testManager);

    private final CommandBlastTrigger command = new CommandBlastTrigger();

    private final Queue<FakeBlast> blastsCreated = new LinkedList();

    @BeforeEach
    public void setupBeforeTest()
    {
        ICBMClassicAPI.EXPLOSIVE_REGISTRY = new ExplosiveRegistry();
        ICBMClassicAPI.EXPLOSIVE_REGISTRY.register(new ResourceLocation("tree", "small"), EnumTier.ONE, () ->
        {
            FakeBlast fakeBlast = new FakeBlast();
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

    @Test
    void command_short() throws CommandException
    {
        final String[] commandArgs = new String[]{"tree:small", "2"};
        command.handleCommand(testManager.getServer(), testManager.getPlayer(), commandArgs);
    }
}
