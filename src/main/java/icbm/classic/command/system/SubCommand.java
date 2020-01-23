package icbm.classic.command.system;

import net.minecraft.command.ICommandSender;
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
public abstract class SubCommand implements ISubCommand
{

    private final String name;
    protected ICommandGroup parent;

    public SubCommand(String name)
    {
        this.name = name;
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public String getUsage(ICommandSender sender)
    {
        if (parent == null)
        {
            return "/" + getName();
        }
        return parent.getUsage(sender) + " " + getName();
    }

    @Override
    public void displayHelp(ICommandSender sender)
    {
        collectHelpForAll((string) -> sendHelpMessage(sender, string));

        //If we have a command sender entity then we can run world based commands
        if (sender.getCommandSenderEntity() != null)
        {
            collectHelpWorldOnly((string) -> sendHelpMessage(sender, string));
        }

        if (sender instanceof MinecraftServer)
        {
            collectHelpServerOnly((string) -> sendHelpMessage(sender, string));
        }
    }

    private void sendHelpMessage(ICommandSender sender, String message)
    {
        sender.sendMessage(new TextComponentString((getUsage(sender) + " " + message).trim()));
    }

    /**
     * Called to supply a list of commands that can be run by any sender type
     *
     * @param consumer - collector
     */
    protected void collectHelpForAll(Consumer<String> consumer)
    {
        consumer.accept("");
    }

    /**
     * Collect commands that can only run in the world
     *
     * @param consumer - collector
     */
    protected void collectHelpWorldOnly(Consumer<String> consumer)
    {

    }

    /**
     * Collect commands that can only run in the several console
     *
     * @param consumer - collector
     */
    protected void collectHelpServerOnly(Consumer<String> consumer)
    {

    }

    @Override
    public List<String> getTabSuggestions(@Nonnull MinecraftServer server, @Nonnull ICommandSender sender, @Nonnull String[] args, @Nullable BlockPos targetPos)
    {
        return Collections.<String>emptyList();
    }

    public void setParent(ICommandGroup parent)
    {
        this.parent = parent;
    }
}
