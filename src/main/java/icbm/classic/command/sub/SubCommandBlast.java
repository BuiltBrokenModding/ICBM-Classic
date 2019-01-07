package icbm.classic.command.sub;

import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.ICBMClassicHelpers;
import icbm.classic.api.reg.IExplosiveData;
import icbm.classic.command.imp.SubCommand;
import icbm.classic.lib.explosive.ExplosiveHandler;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;

/**
 * Created by Dark(DarkGuardsman, Robert) on 4/13/2018.
 */
public class SubCommandBlast extends SubCommand
{

    @Override
    public String getName()
    {
        return "blast";
    }

    @Override
    public String getUsage(ICommandSender sender)
    {
        return "blast";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        if (args.length >= 1 && args[0].equalsIgnoreCase("list"))
        {
            String names = "Explosive Types: ";
            for (IExplosiveData data : ICBMClassicAPI.EXPLOSIVE_REGISTRY.getExplosives())
            {
                names += data.getRegistryName();
                names += ", ";
            }
            sender.sendMessage(new TextComponentString(names.substring(names.length() - 2))); //-2 removes last ', ' ... because lazy
        }
        else if (args.length >= 1 && args[0].equalsIgnoreCase("spread"))
        {
            int spread = parseInt(args[1]);


            for (int x = -spread; x <= spread; x++)
            {
                for (int z = -spread; z <= spread; z++)
                {
                    String[] parms = new String[]{args[2], args[3], args[4] + x * 100, args[5], args[6] + x * 100, args[7]};
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
                x = getNumber(sender, args[2], sender.getPositionVector().x);
                y = getNumber(sender, args[3], sender.getPositionVector().y);
                z = getNumber(sender, args[4], sender.getPositionVector().z);
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

    protected double getNumber(ICommandSender sender, String value, double alt) throws WrongUsageException
    {
        if (value.equals("~"))
        {
            if (!(sender instanceof MinecraftServer))
            {
                return alt;
            }
            throw new WrongUsageException("'~' can't be used from console");
        }
        else if (value.startsWith("~"))
        {
            if (!(sender instanceof MinecraftServer))
            {
                return alt + Double.parseDouble(value.substring(1, value.length()));
            }
            throw new WrongUsageException("'~' can't be used from console");
        }
        else
        {
            return Double.parseDouble(value);
        }
    }
}
