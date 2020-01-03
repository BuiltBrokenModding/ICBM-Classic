package icbm.classic.command;

import com.builtbroken.mc.testing.junit.TestManager;
import icbm.classic.command.imp.SubCommand;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Robert Seifert on 1/2/20.
 */
public class CommandICBMTest
{
    //Entire class
    private static TestManager testManager = new TestManager("CommandUtils");

    //Per Test
    private final DummyCommandSender dummyCommandSender = new DummyCommandSender(testManager);
    private final CommandICBM commandICBM = new CommandICBM("icbm");

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
    void getName()
    {
        Assertions.assertEquals("icbm", commandICBM.getName());
    }

    @Test
    void getUsage()
    {
        Assertions.assertEquals("/icbm", commandICBM.getUsage(null));
    }

    @Test
    void getRequiredPermissionLevel()
    {
        Assertions.assertEquals(2, commandICBM.getRequiredPermissionLevel());
    }

    @Test
    void getTabCompletions_help()
    {
        final DummyCommandSender dummyCommandSender = new DummyCommandSender(testManager);

        List<String> output = commandICBM.getTabCompletions(testManager.getServer(), dummyCommandSender, new String[]{"h"}, null);
        Assertions.assertEquals(1, output.size());
        Assertions.assertEquals(output.get(0), "help");
    }

    @Test
    void getTabCompletions_nothing()
    {
        final DummyCommandSender dummyCommandSender = new DummyCommandSender(testManager);

        List<String> output = commandICBM.getTabCompletions(testManager.getServer(), dummyCommandSender, new String[]{"a"}, null);
        Assertions.assertEquals(0, output.size());
    }

    @Test
    void getTabCompletions_zero()
    {
        final DummyCommandSender dummyCommandSender = new DummyCommandSender(testManager);

        List<String> output = commandICBM.getTabCompletions(testManager.getServer(), dummyCommandSender, new String[0], null);
        Assertions.assertEquals(0, output.size());
    }

    @Test
    void getTabCompletions_something()
    {
        commandICBM.subCommandMap.put("something", new CommandSomething(commandICBM));

        List<String> output = commandICBM.getTabCompletions(testManager.getServer(), dummyCommandSender, new String[]{"something", "t"}, null);
        Assertions.assertEquals(1, output.size());
        Assertions.assertEquals(output.get(0), "tree");
    }

    @Test
    void execute_nothing() throws CommandException
    {
        final DummyCommandSender dummyCommandSender = new DummyCommandSender(testManager);

        //Run command
        commandICBM.execute(testManager.getServer(), dummyCommandSender, new String[0]);

        Assertions.assertEquals(1, dummyCommandSender.messages.size());
        Assertions.assertEquals("/icbm help", dummyCommandSender.messages.poll().getUnformattedText());
    }

    @Test
    void execute_something_noArgs() throws CommandException
    {
        commandICBM.subCommandMap.put("something", new CommandSomething(commandICBM));

        //Run command
        commandICBM.execute(testManager.getServer(), dummyCommandSender, new String[]{"something"});

        Assertions.assertEquals(1, dummyCommandSender.messages.size());
        Assertions.assertEquals("something>", dummyCommandSender.messages.poll().getUnformattedText());
    }

    @Test
    void execute_something_args() throws CommandException
    {
        commandICBM.subCommandMap.put("something", new CommandSomething(commandICBM));

        //Run command
        commandICBM.execute(testManager.getServer(), dummyCommandSender, new String[]{"something", "tree", "bat"});

        Assertions.assertEquals(1, dummyCommandSender.messages.size());
        Assertions.assertEquals("something>tree,bat", dummyCommandSender.messages.poll().getUnformattedText());
    }

    private class CommandSomething extends SubCommand
    {

        public CommandSomething(CommandBase parent)
        {
            super(parent, "something");
        }

        @Override
        public void execute(MinecraftServer server, ICommandSender sender, String[] args)
        {
            String reply = "something>" + Arrays.stream(args).collect(Collectors.joining(","));
            sender.sendMessage(new TextComponentString(reply.trim()));
        }

        @Override
        public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos)
        {
            if (args.length == 1)
            {
                return getListOfStringsMatchingLastWord(args, "tree", "bat", "cat");
            }
            return Collections.<String>emptyList();
        }
    }
}
