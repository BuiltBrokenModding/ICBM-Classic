package icbm.classic.command.sub.blast;

import icbm.classic.api.ICBMClassicHelpers;
import icbm.classic.api.reg.IExplosiveData;
import icbm.classic.command.CommandUtils;
import icbm.classic.command.system.SubCommand;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

/**
 * Created by Robert Seifert on 1/6/20.
 */
public class CommandBlastSpread extends SubCommand
{
    public CommandBlastSpread()
    {
        super("spread");
    }

    @Override
    protected void collectHelpServer(Consumer<String> consumer)
    {
        consumer.accept("<count> <distance> <id> <x> <y> <z> <scale>");
    }

    @Override
    public void handleCommand(@Nonnull MinecraftServer server, @Nonnull ICommandSender sender, @Nonnull String[] args) throws CommandException
    {
        final int count = CommandBase.parseInt(args[1], 1);
        final int distance = CommandBase.parseInt(args[2], 1);

        //Get explosive data from user
        final String explosive_id = args[3];
        final IExplosiveData explosiveData = ICBMClassicHelpers.getExplosive(explosive_id, true);
        if (explosiveData == null)
        {
            throw new WrongUsageException("Could not find explosive by ID [" + explosive_id + "]");
        }

        //Get world position
        final World world = CommandUtils.getWorld(sender, args[4], sender.getEntityWorld());
        final double xInput = CommandUtils.getNumber(sender, args[5], sender.getPosition().getX() + 0.5);
        final double yInput = CommandUtils.getNumber(sender, args[6], sender.getPosition().getX() + 0.5);
        final double zInput = CommandUtils.getNumber(sender, args[7], sender.getPosition().getX() + 0.5);

        //Get scale from user
        final float scale = Float.parseFloat(args[8]);
        if (scale <= 0)
        {
            throw new WrongUsageException("Scale must be greater than zero!");
        }

        //Generate blasts in a grid
        for (int xi = -count; xi <= count; xi++)
        {
            for (int zi = -count; zi <= count; zi++)
            {
                //calc position
                final double x = xInput + xi * distance;
                final double z = zInput + zi * distance;

                //Trigger blast
                CommandBlastTrigger.trigger(sender, world, x, yInput, z, explosiveData, scale);
            }
        }
    }
}
