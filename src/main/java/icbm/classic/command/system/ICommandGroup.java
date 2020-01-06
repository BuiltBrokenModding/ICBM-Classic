package icbm.classic.command.system;

import net.minecraft.command.ICommandSender;

import java.util.Collection;

/**
 * Created by Robert Seifert on 1/6/20.
 */
public interface ICommandGroup extends ISubCommand
{

    /**
     * Gets the list of sub commands in this command group
     *
     * @return list of commands or empty list
     */
    Collection<ISubCommand> getSubCommands();

    /**
     * Registers a command with the group of commands
     *
     * @param command - command to add, needs to have a unique {@link ISubCommand#getName()}
     */
    void registerCommand(ISubCommand command);

    /**
     * Gets the usage of the command
     *
     * @param sender - user of the command
     * @return string usage of the command
     */
    String getUsage(ICommandSender sender);
}
