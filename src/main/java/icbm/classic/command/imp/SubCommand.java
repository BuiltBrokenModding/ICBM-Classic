package icbm.classic.command.imp;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextComponentString;

import java.util.function.Consumer;

/**
 * Created by Dark(DarkGuardsman, Robert) on 4/13/2018.
 */
public abstract class SubCommand extends CommandBase
{

    private final String name;
    private CommandBase parent;

    public SubCommand(CommandBase parent, String name)
    {
        this.parent = parent;
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
        return parent.getUsage(sender) + " " + getName();
    }

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
}
