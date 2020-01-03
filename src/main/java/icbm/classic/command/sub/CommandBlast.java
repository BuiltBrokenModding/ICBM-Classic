package icbm.classic.command.sub;

import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.ICBMClassicHelpers;
import icbm.classic.api.reg.IExplosiveData;
import icbm.classic.command.CommandUtils;
import icbm.classic.command.imp.SubCommand;
import icbm.classic.lib.explosive.ExplosiveHandler;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;

import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Created by Dark(DarkGuardsman, Robert) on 4/13/2018.
 */
public class CommandBlast extends SubCommand
{
    public CommandBlast(CommandBase parent)
    {
        super(parent, "blast");
    }

    @Override
    protected void collectHelpServer(Consumer<String> consumer)
    {
        consumer.accept("list");
        consumer.accept("<id> <x> <y> <z> <scale>");
        consumer.accept("spread <amount> <id> <x> <y> <z> <scale>");
    }

    @Override
    protected void collectHelpPlayer(Consumer<String> consumer)
    {
        consumer.accept("<id> <scale>");
    }

    protected void listBlasts(ICommandSender sender) {
        //Convert list of explosives to string registry names
        String names = ICBMClassicAPI.EXPLOSIVE_REGISTRY.getExplosives().stream()
                .map(IExplosiveData::getRegistryName)
                .map(ResourceLocation::toString)
                .sorted()
                .collect(Collectors.joining(", "));

        //Output message TODO translate if possible?
        sender.sendMessage(new TextComponentString("Explosive Types: " + names ));
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        if (args.length >= 1 && args[0].equalsIgnoreCase("list"))
        {
            listBlasts(sender);
        }
        else if (args.length >= 1 && args[0].equalsIgnoreCase("spread"))
        {
           final int spread = parseInt(args[1]);


            for (int x = -spread; x <= spread; x++)
            {
                for (int z = -spread; z <= spread; z++)
                {
                    //TODO split array rather than manually converting *facepalm*
                    String[] parms = new String[]{args[2], args[3], args[4] + x * 100, args[5], args[6] + z * 100, args[7]};
                    execute(server, sender, parms);
                }
            }
        }
        else if (args.length >= 2)
        {
            final String explosive_id = args[0];
            final float scale = Float.parseFloat(args.length == 2 ? args[1] : args[5]);
            if (scale <= 0)
            {
                throw new WrongUsageException("Scale must be greater than zero!");
            }

            final IExplosiveData explosiveData = ICBMClassicHelpers.getExplosive(explosive_id, true);

            if (explosiveData  == null)
            {
                throw new WrongUsageException("Could not find explosive by ID [" + explosive_id + "]");
            }

            //Get position
            World world;
            double x, y, z;
            if (args.length == 6)
            {
                world = args[1].equals("~") && !(sender instanceof MinecraftServer) ? sender.getEntityWorld() : DimensionManager.getWorld(Integer.getInteger(args[1]));
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

}
