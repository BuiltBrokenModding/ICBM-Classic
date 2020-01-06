package icbm.classic.command.sub.blast;

import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.ICBMClassicHelpers;
import icbm.classic.api.reg.IExplosiveData;
import icbm.classic.command.CommandUtils;
import icbm.classic.command.system.SubCommand;
import icbm.classic.lib.explosive.ExplosiveHandler;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Created by Robert Seifert on 1/6/20.
 */
public class CommandBlastTrigger extends SubCommand
{
    public CommandBlastTrigger()
    {
        super("trigger");
    }

    @Override
    protected void collectHelpServer(Consumer<String> consumer)
    {
        consumer.accept("<id> <x> <y> <z> <scale>");
    }

    @Override
    protected void collectHelpPlayer(Consumer<String> consumer)
    {
        consumer.accept("<id> <scale>");
    }

    @Override
    public void handleCommand(@Nonnull MinecraftServer server, @Nonnull ICommandSender sender, @Nonnull String[] args) throws CommandException
    {
        final String explosive_id = args[0];
        final float scale = Float.parseFloat(args.length == 2 ? args[1] : args[5]);
        if (scale <= 0)
        {
            throw new WrongUsageException("Scale must be greater than zero!");
        }

        final IExplosiveData explosiveData = ICBMClassicHelpers.getExplosive(explosive_id, true);

        if (explosiveData == null)
        {
            throw new WrongUsageException("Could not find explosive by ID [" + explosive_id + "]");
        }

        //Get position
        World world;
        double x, y, z;
        if (args.length == 6)
        {
            world = CommandUtils.getWorld(sender, args[1], sender.getEntityWorld());
            x = CommandUtils.getNumber(sender, args[2], sender.getPositionVector().x);
            y = CommandUtils.getNumber(sender, args[3], sender.getPositionVector().y);
            z = CommandUtils.getNumber(sender, args[4], sender.getPositionVector().z);
        }
        else if (!(sender instanceof MinecraftServer))
        {
            world = sender.getEntityWorld();
            x = sender.getPositionVector().x;
            y = sender.getPositionVector().y;
            z = sender.getPositionVector().z;
        }
        else
        {
            throw new WrongUsageException("/icbmc remove <all/missile/explosion> dim_id x y z radius");
        }

        if (world != null)
        {
            ExplosiveHandler.createExplosion(null, world, x, y, z, explosiveData.getRegistryID(), scale, null);
            sender.sendMessage(new TextComponentString("Generated blast with explosive [" + explosiveData.getRegistryName() + "] with scale " + scale + " at location " + new BlockPos(x, y, z)));
        }
        else
        {
            throw new WrongUsageException("Failed to get a world instance from arguments or sender.");
        }
    }
}
