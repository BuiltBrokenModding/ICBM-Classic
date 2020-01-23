package icbm.classic.command.system;

import com.builtbroken.mc.testing.junit.TestManager;
import com.builtbroken.mc.testing.junit.testers.DummyCommandSender;
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
public class CommandEntryPointTest
{
    //Entire class
    private static TestManager testManager = new TestManager("CommandUtils", Assertions::fail);

    //Per Test
    private final DummyCommandSender dummyCommandSender = new DummyCommandSender(testManager);
    private final CommandGroup commandGroup = new CommandGroup("icbm");
    private final CommandEntryPoint commandHandler = new CommandEntryPoint("icbm", commandGroup);

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
        Assertions.assertEquals("icbm", commandHandler.getName());
    }

    @Test
    void getUsage()
    {
        Assertions.assertEquals("/icbm", commandHandler.getUsage(null));
    }

    @Test
    void getRequiredPermissionLevel()
    {
        Assertions.assertEquals(2, commandHandler.getRequiredPermissionLevel());
    }

    @Test
    void getTabCompletions_help()
    {
        final DummyCommandSender dummyCommandSender = new DummyCommandSender(testManager);

        List<String> output = commandHandler.getTabCompletions(testManager.getServer(), dummyCommandSender, new String[]{"h"}, null);
        Assertions.assertEquals(1, output.size());
        Assertions.assertEquals(output.get(0), "help");
    }

    @Test
    void getTabCompletions_nothing()
    {
        final DummyCommandSender dummyCommandSender = new DummyCommandSender(testManager);

        List<String> output = commandHandler.getTabCompletions(testManager.getServer(), dummyCommandSender, new String[]{"a"}, null);
        Assertions.assertEquals(0, output.size());
    }

    @Test
    void getTabCompletions_zero()
    {
        final DummyCommandSender dummyCommandSender = new DummyCommandSender(testManager);

        List<String> output = commandHandler.getTabCompletions(testManager.getServer(), dummyCommandSender, new String[0], null);
        Assertions.assertEquals(0, output.size());
    }

    @Test
    void getTabCompletions_something()
    {
        commandGroup.registerCommand(new CommandSomething());

        List<String> output = commandHandler.getTabCompletions(testManager.getServer(), dummyCommandSender, new String[]{"something", "t"}, null);
        Assertions.assertEquals(1, output.size());
        Assertions.assertEquals(output.get(0), "tree");
    }

    @Test
    void execute_nothing() throws CommandException
    {
        final DummyCommandSender dummyCommandSender = new DummyCommandSender(testManager);

        //Run command
        commandHandler.execute(testManager.getServer(), dummyCommandSender, new String[0]);

        Assertions.assertEquals(1, dummyCommandSender.messages.size());
        Assertions.assertEquals("/icbm help", dummyCommandSender.messages.poll().getUnformattedText());
    }

    @Test
    void execute_something_noArgs() throws CommandException
    {
        commandGroup.registerCommand(new CommandSomething());

        //Run command
        commandHandler.execute(testManager.getServer(), dummyCommandSender, new String[]{"something"});

        Assertions.assertEquals(1, dummyCommandSender.messages.size());
        Assertions.assertEquals("something>", dummyCommandSender.messages.poll().getUnformattedText());
    }

    @Test
    void execute_something_args() throws CommandException
    {
        commandGroup.registerCommand(new CommandSomething());

        //Run command
        commandHandler.execute(testManager.getServer(), dummyCommandSender, new String[]{"something", "tree", "bat"});

        Assertions.assertEquals(1, dummyCommandSender.messages.size());
        Assertions.assertEquals("something>tree,bat", dummyCommandSender.messages.poll().getUnformattedText());
    }

    private static class CommandSomething extends SubCommand
    {
        public CommandSomething()
        {
            super("something");
        }

        @Override
        public void handleCommand(MinecraftServer server, ICommandSender sender, String[] args)
        {
            String reply = "something>" + Arrays.stream(args).collect(Collectors.joining(","));
            sender.sendMessage(new TextComponentString(reply.trim()));
        }

        @Override
        public List<String> getTabSuggestions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos)
        {
            if (args.length == 1)
            {
                return CommandBase.getListOfStringsMatchingLastWord(args, "tree", "bat", "cat");
            }
            return Collections.<String>emptyList();
        }
    }
}
