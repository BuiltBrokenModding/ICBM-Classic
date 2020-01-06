package icbm.classic.command.sub;

import icbm.classic.command.system.ICommandGroup;
import icbm.classic.command.system.SubCommand;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

import javax.annotation.Nonnull;

/**
 * Created by Robert Seifert on 1/6/20.
 */
public class CommandHelp extends SubCommand
{

    public CommandHelp()
    {
        super("help");
    }

    @Override
    public void handleCommand(@Nonnull MinecraftServer server, @Nonnull ICommandSender sender, @Nonnull String[] args)
    {
        parent.getSubCommands().forEach(command -> command.displayHelp(sender));
    }
}
