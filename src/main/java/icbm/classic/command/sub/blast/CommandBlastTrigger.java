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
    protected void collectHelpForAll(Consumer<String> consumer)
    {
        consumer.accept("<id> <x> <y> <z> <scale>");
    }

    @Override
    protected void collectHelpWorldOnly(Consumer<String> consumer)
    {
        consumer.accept("<id> <scale>");
    }

    @Override
    public void handleCommand(@Nonnull MinecraftServer server, @Nonnull ICommandSender sender, @Nonnull String[] args) throws CommandException
    {
        //Get explosive from user
        final String explosive_id = args[0];
        final IExplosiveData explosiveData = ICBMClassicHelpers.getExplosive(explosive_id, true);
        if (explosiveData == null)
        {
            throw new WrongUsageException("Could not find explosive by ID [" + explosive_id + "]");
        }

        if (args.length == 6)
        {
            longVersion(sender, explosiveData, args);
        }
        else if (!(sender instanceof MinecraftServer) && args.length == 2)
        {
           shortVersion(sender, explosiveData, args);
        }
        else
        {
            throw new WrongUsageException("/icbmc remove <all/missile/explosion> dim_id x y z radius");
        }
    }

    private void shortVersion(ICommandSender sender, IExplosiveData explosiveData, String[] args) throws WrongUsageException
    {
        final float scale = Float.parseFloat(args[1]);
        if (scale <= 0)
        {
            throw new WrongUsageException("Scale must be greater than zero!");
        }

        //Get position data
        final World world = sender.getEntityWorld();
        final double x = sender.getPositionVector().x;
        final double y = sender.getPositionVector().y;
        final double z = sender.getPositionVector().z;

        //Trigger blast
        trigger(sender, world, x, y, z, explosiveData, scale);
    }

    private void longVersion(ICommandSender sender, IExplosiveData explosiveData, String[] args) throws WrongUsageException
    {
        final float scale = Float.parseFloat(args[1]);
        if (scale <= 0)
        {
            throw new WrongUsageException("Scale must be greater than zero!");
        }

        //Get position data
        final World world = CommandUtils.getWorld(sender, args[5], sender.getEntityWorld());
        final double x = CommandUtils.getNumber(sender, args[2], sender.getPositionVector().x);
        final double y = CommandUtils.getNumber(sender, args[3], sender.getPositionVector().y);
        final double z = CommandUtils.getNumber(sender, args[4], sender.getPositionVector().z);

        //Trigger blast
        trigger(sender, world, x, y, z, explosiveData, scale);
    }

    public static void trigger(ICommandSender sender, World world, double x, double y, double z, IExplosiveData explosiveData, float scale) {
        ExplosiveHandler.createExplosion(null, world, x, y, z, explosiveData.getRegistryID(), scale, null);
        sender.sendMessage(new TextComponentString("Generated blast with explosive [" + explosiveData.getRegistryName() + "] with scale " + scale + " at location " + new BlockPos(x, y, z)));
    }
}
