package icbm.classic.command.sub.blast;

import com.builtbroken.mc.testing.junit.TestManager;
import com.builtbroken.mc.testing.junit.testers.DummyCommandSender;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Created by Robert Seifert on 1/6/20.
 */
public class CommandBlastSpreadTest
{
    private static TestManager testManager = new TestManager("CommandBlastSpreadTest", Assertions::fail);
    private final DummyCommandSender dummyCommandSender = new DummyCommandSender(testManager);

    private final CommandBlastSpread command = new CommandBlastSpread();

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
        Assertions.assertEquals("/spread <count> <distance> <id> <x> <y> <z> <scale>", dummyCommandSender.pollLastMessage());
    }

    @Test
    void help_player()
    {
        command.displayHelp(testManager.getPlayer());
        Assertions.assertEquals(1, testManager.getPlayer().messages.size());
        Assertions.assertEquals("/spread <count> <distance> <id> <x> <y> <z> <scale>", testManager.getPlayer().pollLastMessage());
    }
}
