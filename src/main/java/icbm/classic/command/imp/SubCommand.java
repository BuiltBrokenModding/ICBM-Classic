package icbm.classic.command.imp;

import com.sun.istack.internal.NotNull;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

/**
 * Created by Dark(DarkGuardsman, Robert) on 4/13/2018.
 */
public abstract class SubCommand
{
    private final String name;
    private CommandBase parent;

    public SubCommand(CommandBase parent, String name)
    {
        this.parent = parent;
        this.name = name;
    }

    public String getName()
    {
        return name;
    }

    public String getUsage(ICommandSender sender)
    {
        return parent.getUsage(sender) + " " + getName();
    }

    public abstract void handleCommand(@Nonnull MinecraftServer server, @Nonnull ICommandSender sender, @Nonnull String[] args) throws CommandException;

    /**
     * Sends all help text to the sender
     * @param sender
     */
    public void displayHelp(ICommandSender sender)
    {
        collectHelpServer((string) -> sender.sendMessage(new TextComponentString((getUsage(sender) + " " + string).trim())));
        if (sender instanceof EntityPlayer)
        {
            collectHelpPlayer((string) -> sender.sendMessage(new TextComponentString((getUsage(sender) + " " + string).trim())));
        }
    }

    protected void collectHelpServer(Consumer<String> consumer)
    {
        consumer.accept("");
    }

    protected void collectHelpPlayer(Consumer<String> consumer)
    {

    }

    /**
     * Gets a list of suggestions for completing the current command
     * @param server - server running the command
     * @param sender - user triggering the command
     * @param args - arguments for the command
     * @param targetPos - block position for the command
     * @return empty list or list containing suggestions
     */
    public List<String> getTabSuggestions(@Nonnull MinecraftServer server, @Nonnull ICommandSender sender, @Nonnull String[] args, @Nullable BlockPos targetPos)
    {
        return Collections.<String>emptyList();
    }
}
