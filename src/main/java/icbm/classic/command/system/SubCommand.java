package icbm.classic.command.system;

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
        if(parent == null) {
            return "/" + getName();
        }
        return parent.getUsage(sender) + " " + getName();
    }

    @Override
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
