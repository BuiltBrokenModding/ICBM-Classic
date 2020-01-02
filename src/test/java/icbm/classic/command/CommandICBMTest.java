package icbm.classic.command;

import com.builtbroken.mc.testing.junit.TestManager;
import net.minecraft.command.CommandException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Created by Robert Seifert on 1/2/20.
 */
public class CommandICBMTest
{
    @Test
    void execute_nothing() throws CommandException
    {
        CommandICBM commandICBM = new CommandICBM("icbm");

        //Build server
        TestManager testManager = new TestManager("CommandICBM_nothing");

        //Build sender
        DummyCommandSender dummyCommandSender = new DummyCommandSender();
        dummyCommandSender.world = testManager.getWorld();
        dummyCommandSender.server = testManager.getServer();

        //Run command
        commandICBM.execute(testManager.getServer(), dummyCommandSender, new String[0]);

        Assertions.assertEquals("/icbm help", dummyCommandSender.messages.poll().getUnformattedText());
    }
}
