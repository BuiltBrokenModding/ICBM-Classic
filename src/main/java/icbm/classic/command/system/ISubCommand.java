package icbm.classic.command.system;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * Created by Robin Seifert on 1/6/20.
 *
 * PORT: 1.21 commands run through Brigadier. Each sub command is bridged into a Brigadier literal by
 * {@link CommandEntryPoint}, with its raw arguments passed in as a string array exactly like 1.12.
 */
public interface ISubCommand
{

    /**
     * Called to run the command
     *
     * @param server - server handling the command
     * @param sender - user triggering the command
     * @param args   - arguments for the command
     * @throws CommandSyntaxException - failed to run the command
     */
    void handleCommand(@Nonnull MinecraftServer server, @Nonnull CommandSourceStack sender, @Nonnull String[] args) throws CommandSyntaxException;

    /**
     * Sends all help text to the sender
     *
     * @param sender
     */
    default void displayHelp(CommandSourceStack sender)
    {
        sender.sendSystemMessage(Component.literal(getUsage(sender)));
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
    List<String> getTabSuggestions(@Nonnull MinecraftServer server, @Nonnull CommandSourceStack sender, @Nonnull String[] args, @Nullable BlockPos targetPos);


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
    String getUsage(CommandSourceStack sender);

    /**
     * Sets the parent of the command
     *
     * @param parent - command's parent
     */
    void setParent(ICommandGroup parent);
}
// PORTED
