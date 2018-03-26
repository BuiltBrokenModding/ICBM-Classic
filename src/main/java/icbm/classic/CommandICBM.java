package icbm.classic;

import icbm.classic.content.entity.EntityExplosive;
import icbm.classic.content.entity.EntityFlyingBlock;
import icbm.classic.content.entity.EntityFragments;
import icbm.classic.content.entity.missile.EntityMissile;
import net.minecraft.command.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class CommandICBM extends CommandBase
{
    @Override
    public String getName()
    {
        return "icbmc";
    }

    @Override
    public String getUsage(ICommandSender sender)
    {
        return "/icbmc";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        if (args.length == 0 || args[0].equalsIgnoreCase("help"))
        {
            displayHelp(sender, false);
        }
        else if (args[0].equalsIgnoreCase("remove"))
        {
            commandRemove(server, sender, args);
        }
        else if (args[0].equalsIgnoreCase("blast"))
        {
            commandBlast(server, sender, args);
        }
        else if (args[0].equalsIgnoreCase("lag"))
        {
            commandLag(server, sender, args);
        }
        else
        {
            sender.sendMessage(new TextComponentString("\u00a7c" + "Unknown command! Use '" + getUsage(sender) + " help' for more a list of commands"));
        }
    }

    protected void displayHelp(ICommandSender sender, boolean error)
    {
        if (sender instanceof EntityPlayer)
        {
            sender.sendMessage(new TextComponentString((error ? "\u00a7c" : "") + "/icbmc blast <id> <scale>"));
            sender.sendMessage(new TextComponentString((error ? "\u00a7c" : "") + "/icbmc blast <x> <y> <z> <scale>"));
            sender.sendMessage(new TextComponentString((error ? "\u00a7c" : "") + "/icbmc remove <all/missiles/explosions> [radius]"));
            sender.sendMessage(new TextComponentString((error ? "\u00a7c" : "") + "/icbmc remove <all/missiles/explosions> <x> <y> <z> <radius>"));
            sender.sendMessage(new TextComponentString((error ? "\u00a7c" : "") + "/icbmc lag [radius]"));
        }
        else
        {
            sender.sendMessage(new TextComponentString((error ? "\u00a7c" : "") + "/icbmc blast <x> <y> <z> <scale>"));
            sender.sendMessage(new TextComponentString((error ? "\u00a7c" : "") + "/icbmc remove <all/missiles/explosions> <x> <y> <z> <radius>"));
        }
    }

    protected void commandBlast(MinecraftServer server, ICommandSender sender, String[] args) throws WrongUsageException
    {
        //TODO will implement this later
        sender.sendMessage(new TextComponentString("\u00a7cCommand not implement yet!"));
    }

    protected void commandRemove(MinecraftServer server, ICommandSender sender, String[] args) throws WrongUsageException
    {
        if (args.length >= 2)
        {
            //Get type
            final String type_arg = args[1];
            boolean remove_all = type_arg.equalsIgnoreCase("all");
            boolean remove_missiles = remove_all
                    || type_arg.equalsIgnoreCase("missiles")
                    || type_arg.equalsIgnoreCase("missile");
            boolean remove_explosives = remove_all
                    || type_arg.equalsIgnoreCase("explosions")
                    || type_arg.equalsIgnoreCase("explosion")
                    || type_arg.equalsIgnoreCase("explosive")
                    || type_arg.equalsIgnoreCase("explosives")
                    || type_arg.equalsIgnoreCase("ex");

            //Get output string
            final String typeString = remove_all ? "entities" : remove_missiles ? "missiles" : remove_explosives ? "explosions" : null;
            if (typeString != null)
            {
                //Get range
                boolean hasRange = args.length == 3 || args.length == 6;
                int range = args.length == 3 ? parseRadius(args[2])
                        : args.length == 6 ? parseRadius(args[5])
                        : -1;

                //Get position
                double x, y, z;
                if (sender instanceof MinecraftServer)
                {
                    if (args.length == 6)
                    {
                        x = Double.parseDouble(args[2]);
                        y = Double.parseDouble(args[3]);
                        z = Double.parseDouble(args[4]);
                    }
                    else
                    {
                        throw new WrongUsageException("/icbmc remove <all/missile/explosion> x y z radius");
                    }
                }
                else
                {
                    x = sender.getPositionVector().x;
                    y = sender.getPositionVector().y;
                    z = sender.getPositionVector().z;
                }

                //Get entities
                List<Entity> entities = getEntities(sender, x, y, z, range);

                int count = 0;

                //Loop with for-loop to prevent CME
                for (int i = 0; i < entities.size(); i++)
                {
                    Entity entity = entities.get(i);
                    if (entity != null && !entity.isDead)
                    {
                        boolean isExplosive = entity instanceof EntityExplosive;
                        boolean isMissile = entity instanceof EntityMissile;
                        boolean isICBM = entity instanceof EntityFragments || entity instanceof EntityFlyingBlock;
                        if (remove_explosives && isExplosive || remove_missiles && isMissile || remove_all && isICBM)
                        {
                            entity.setDead();
                            count++;
                        }
                    }
                }

                sender.sendMessage(new TextComponentString("Removed '" + count + "' ICBM entities "  + (hasRange ? "within " + range + "meters" : "from the world")));
                return; //Necessary return
            }

        }
        throw new WrongUsageException("'/icbmc remove <all/missile/explosion> [radius]' or '/icbmc remove <all/missile/explosion> <x> <y> <z> <radius>'");
    }

    protected void commandLag(MinecraftServer server, ICommandSender sender, String[] args) throws WrongUsageException
    {
        double range = args.length > 1 ? Double.parseDouble(args[1]) : 1000;

        //Get entities
        List<Entity> entities = getEntities(sender, sender.getPositionVector().x, sender.getPositionVector().y, sender.getPositionVector().z, range);

        int count = 0;
        //Loop with for-loop to prevent CME
        for (int i = 0; i < entities.size(); i++)
        {
            Entity entity = entities.get(i);
            if (entity != null && !entity.isDead)
            {
                if (entity instanceof EntityFragments || entity instanceof EntityFlyingBlock || entity instanceof EntityMissile || entity instanceof EntityExplosive)
                {
                    entity.setDead();
                    count++;
                }
            }
        }
        sender.sendMessage(new TextComponentString("Removed '" + count + "' sources of lag caused by ICBM within " + range + " meters"));
    }

    protected List<Entity> getEntities(ICommandSender sender, double x, double y, double z, double range)
    {
        if (range > 0)
        {
            AxisAlignedBB bb = new AxisAlignedBB(
                    x - range, y - range, z - range,
                    x + range, y + range, z + range);

            return sender.getEntityWorld().getEntitiesWithinAABB(Entity.class, bb);
        }
        else if (range == -1)
        {
            return sender.getEntityWorld().loadedEntityList;
        }
        return new ArrayList();
    }

    private int parseRadius(String input) throws WrongUsageException
    {
        try
        {
            int radius = Integer.parseInt(input);
            if (radius <= 0)
            {
                throw new WrongUsageException("Radius must be greater than zero!");
            }
            return radius;
        }
        catch (NumberFormatException e)
        {
            throw new WrongUsageException("Invalid radius!");
        }
    }

    @Override
    public int getRequiredPermissionLevel()
    {
        return 2;
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos)
    {
        return args.length == 1 ? getListOfStringsMatchingLastWord(args, "remove", "emp")
                : args.length == 2 && args[0].equalsIgnoreCase("remove") ? getListOfStringsMatchingLastWord(args, "all", "missile", "explosion") : new ArrayList<>();
    }

    @Override
    public int compareTo(ICommand par1Obj)
    {
        return super.compareTo(par1Obj);
    }

}
