package icbm.classic.command.system;

import icbm.classic.command.ICBMCommands;
import icbm.classic.command.sub.CommandBlast;
import icbm.classic.command.sub.CommandLag;
import icbm.classic.command.sub.CommandRemove;
import net.minecraft.command.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import java.util.*;

/**
 * Entry point for all ICBM commands
 */
public class CommandEntryPoint extends CommandBase
{
    public final String id;
    public final ICommandGroup commandGroup;

    public CommandEntryPoint(String id, ICommandGroup commandGroup)
    {
        this.id = id;
        this.commandGroup = commandGroup;
    }

    @Override
    public String getName()
    {
        return id;
    }

    @Override
    public String getUsage(ICommandSender sender)
    {
        return "/" + id;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        commandGroup.handleCommand(server, sender, args);
    }

    @Override
    public int getRequiredPermissionLevel()
    {
        return 2;
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos)
    {
        return commandGroup.getTabSuggestions(server, sender, args, targetPos);
    }
}
