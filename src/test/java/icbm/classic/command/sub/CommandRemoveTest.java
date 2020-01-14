package icbm.classic.command.sub;

import com.builtbroken.mc.testing.junit.TestManager;
import com.builtbroken.mc.testing.junit.testers.DummyCommandSender;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

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
}
