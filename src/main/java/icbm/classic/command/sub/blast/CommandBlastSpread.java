package icbm.classic.command.sub.blast;

import icbm.classic.api.explosion.BlastState;
import icbm.classic.api.reg.IExplosiveData;
import icbm.classic.command.CommandUtils;
import icbm.classic.command.ICBMCommands;
import icbm.classic.command.system.SubCommand;
import icbm.classic.lib.explosive.ExplosiveHandler;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

/**
 * Created by Robert Seifert on 1/6/20.
 */
public class CommandBlastSpread extends SubCommand
{
    private static final String TRANSLATION_KEY = "command.icbmclassic:icbm.spread";
    public static final String TRANSLATION_SPREAD_START = TRANSLATION_KEY + ".started";

    public CommandBlastSpread()
    {
        super("spread");
    }

    @Override
    protected void collectHelpForAll(Consumer<String> consumer)
    {
        consumer.accept("<count> <distance> <id> <dim> <x> <y> <z> <scale>");
    }

    @Override
    public void handleCommand(@Nonnull MinecraftServer server, @Nonnull ICommandSender sender, @Nonnull String[] args) throws CommandException
    {
        if (args.length == 8)
        {
            doCommand(sender, args);
        }
        else
        {
            throw new WrongUsageException(ICBMCommands.TRANSLATION_UNKNOWN_COMMAND, getUsage(sender));
        }
    }

    private void doCommand(@Nonnull ICommandSender sender, @Nonnull String[] args) throws CommandException
    {
        final int count = CommandBase.parseInt(args[0], 1);
        final int distance = CommandBase.parseInt(args[1], 1);

        //Get explosive data from user
        final IExplosiveData explosiveData = CommandBlastTrigger.getExplosive(args[2]);

        //Get world position
        final World world = CommandUtils.getWorld(sender, args[3], sender.getEntityWorld());
        final double xInput = CommandUtils.getNumber(sender, args[4], sender.getPosition().getX() + 0.5);
        final double yInput = CommandUtils.getNumber(sender, args[5], sender.getPosition().getY() + 0.5);
        final double zInput = CommandUtils.getNumber(sender, args[6], sender.getPosition().getZ() + 0.5);

        //Get scale from user
        final float scale = Float.parseFloat(args[7]); //TODO turn into helper method
        if (scale <= 0)
        {
            throw new SyntaxErrorException(CommandBlastTrigger.TRANSLATION_ERROR_SCALE_ZERO);
        }

        final int expectedSpawnCount = (int)Math.floor(Math.pow((count * 2) + 1, 2));

        sender.sendMessage(new TextComponentTranslation(TRANSLATION_SPREAD_START,
                explosiveData.getRegistryName(), scale,
                world.provider.getDimension(), world.getWorldType().getName(),
                xInput, yInput, zInput,
                count, distance,
                expectedSpawnCount));

        //Generate blasts in a grid
        for (int xi = -count; xi <= count; xi++)
        {
            for (int zi = -count; zi <= count; zi++)
            {
                //calc position
                final double x = xInput + xi * distance;
                final double z = zInput + zi * distance;

                //Trigger blast
                final BlastState result = ExplosiveHandler.createExplosion(null,
                        world, x, yInput, z,
                        explosiveData.getRegistryID(), scale,
                        null);

                if(result != BlastState.TRIGGERED && result != BlastState.THREADING)
                {
                    //Send translated message to user
                    sender.sendMessage(new TextComponentTranslation(CommandBlastTrigger.getTranslationKey(result),
                            explosiveData.getRegistryName(), scale,
                            world.provider.getDimension(), world.getWorldType().getName(),
                            x, yInput, z));
                }
            }
        }
    }
}
