package icbm.classic.command.sub;

import icbm.classic.command.CommandUtils;
import icbm.classic.command.system.ICommandGroup;
import icbm.classic.command.system.SubCommand;
import icbm.classic.lib.explosive.ExplosiveHandler;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;

import java.util.List;
import java.util.function.Consumer;

/**
 * Created by Dark(DarkGuardsman, Robert) on 4/13/2018.
 */
public class CommandLag extends SubCommand
{

    public CommandLag()
    {
        super("lag");
    }

    @Override
    protected void collectHelpForAll(Consumer<String> consumer)
    {
    }

    @Override
    protected void collectHelpWorldOnly(Consumer<String> consumer)
    {
        consumer.accept("lag [radius]");
    }

    @Override
    public void handleCommand(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        //Parse range
        double range = args.length > 1 ? Double.parseDouble(args[1]) : 1000;

        //Get entities
        List<Entity> entities = CommandUtils.getEntities(sender.getEntityWorld(), sender.getPositionVector().x, sender.getPositionVector().y, sender.getPositionVector().z, range);

        int count = 0;
        //Loop with for-loop to prevent CME
        for (int i = 0; i < entities.size(); i++)
        {
            Entity entity = entities.get(i);
            if (entity != null && !entity.isDead)
            {
                if (CommandUtils.isICBMEntity(entity))
                {
                    entity.setDead();
                    count++;
                }
            }
        }

        //Clear blasts
        int blasts = ExplosiveHandler.removeNear(sender.getEntityWorld(), sender.getPositionVector().x, sender.getPositionVector().y, sender.getPositionVector().z, range);

        //Update user with data
        sender.sendMessage(new TextComponentString("Removed '" + count + "' ICBM entities within " + range + " meters"));
        sender.sendMessage(new TextComponentString("Removed '" + blasts + "' blast controllers within " + range + " meters"));

    }
}
