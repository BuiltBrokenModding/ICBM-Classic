package icbm.classic.command.system;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * Created by Robert Seifert on 1/6/20.
 */
public interface ISubCommand
{

    /**
     * Called to run the command
     *
     * @param server - server handling the command
     * @param sender - user triggering the command
     * @param args   - arguments for the command
     * @throws CommandException - failed to run the command
     */
    void handleCommand(@Nonnull MinecraftServer server, @Nonnull ICommandSender sender, @Nonnull String[] args) throws CommandException;

    /**
     * Sends all help text to the sender
     *
     * @param sender
     */
    default void displayHelp(ICommandSender sender)
    {
        sender.sendMessage(new TextComponentString(getUsage(sender)));
    }

    /**
     * Gets a list of suggestions for completing the current command
     *
     * @param server    - server running the command
     * @param sender    - user triggering the command
     * @param args      - arguments for the command
     * @param targetPos - block position for the command
     * @return empty list or list containing suggestions
     */
    List<String> getTabSuggestions(@Nonnull MinecraftServer server, @Nonnull ICommandSender sender, @Nonnull String[] args, @Nullable BlockPos targetPos);


    /**
     * Name of the command
     *
     * @return string name
     */
    String getName();

    /**
     * Usage of the command
     *
     * @param sender - sender of the command
     * @return usage string (/parent + name)
     */
    String getUsage(ICommandSender sender);

    /**
     * Sets the parent of the command
     *
     * @param parent - command's parent
     */
    void setParent(ICommandGroup parent);
}
