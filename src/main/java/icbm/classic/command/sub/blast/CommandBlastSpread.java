package icbm.classic.command.sub.blast;

import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.reg.IExplosiveData;
import icbm.classic.command.CommandUtils;
import icbm.classic.command.system.SubCommand;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentString;

import javax.annotation.Nonnull;
import java.util.function.Consumer;
import java.util.stream.Collectors;

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
        final int count = CommandBase.parseInt(args[1]);
        final int distance = CommandBase.parseInt(args[2]);

        final String blastID = args[3];
        final String dim = args[4];
        final double xInput = CommandUtils.getNumber(sender, args[5], sender.getPosition().getX() + 0.5);
        final double yInput = CommandUtils.getNumber(sender, args[6], sender.getPosition().getX() + 0.5);
        final double zInput = CommandUtils.getNumber(sender, args[7], sender.getPosition().getX() + 0.5);
        final String scale = args[8];

        for (int x = -count; x <= count; x++)
        {
            for (int z = -count; z <= count; z++)
            {
                String[] parms = new String[]{
                        blastID,
                        dim,
                        Double.toString(xInput + x * distance),
                        Double.toString(yInput),
                        Double.toString(zInput + z * distance),
                        scale};
                handleCommand(server, sender, parms);
            }
        }
    }
}
