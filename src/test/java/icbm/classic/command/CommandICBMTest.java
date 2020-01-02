package icbm.classic.command;

import com.builtbroken.mc.testing.junit.TestManager;
import net.minecraft.command.CommandException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Created by Robert Seifert on 1/2/20.
 */
public class CommandICBMTest
{
    private static TestManager testManager = new TestManager("CommandUtils");

    @AfterEach
    public void cleanupBetweenTests() {
        testManager.cleanupBetweenTests();
    }

    @AfterAll
    public static void tearDown() {
        testManager.tearDownTest();
    }

    @Test
    void execute_nothing() throws CommandException
    {
        CommandICBM commandICBM = new CommandICBM("icbm");

        //Build sender
        DummyCommandSender dummyCommandSender = new DummyCommandSender();
        dummyCommandSender.world = testManager.getWorld();
        dummyCommandSender.server = testManager.getServer();

        //Run command
        commandICBM.execute(testManager.getServer(), dummyCommandSender, new String[0]);

        Assertions.assertEquals(1, dummyCommandSender.messages.size());
        Assertions.assertEquals("/icbm help", dummyCommandSender.messages.poll().getUnformattedText());
    }
}
