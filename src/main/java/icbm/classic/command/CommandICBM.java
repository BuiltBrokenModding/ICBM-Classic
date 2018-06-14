package icbm.classic.command;

import icbm.classic.command.imp.SubCommand;
import icbm.classic.command.sub.SubCommandBlast;
import icbm.classic.content.entity.EntityExplosive;
import icbm.classic.content.entity.EntityFlyingBlock;
import icbm.classic.content.entity.EntityFragments;
import icbm.classic.content.entity.missile.EntityMissile;
import icbm.classic.content.explosive.ExplosiveHandler;
import net.minecraft.command.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;

import javax.annotation.Nullable;
import java.util.*;

public class CommandICBM extends CommandBase
{
    private final Map<String, SubCommand> subCommandMap = new HashMap();
    private final String id;

    public CommandICBM(String id)
    {
        this.id = id;
        subCommandMap.put("blast", new SubCommandBlast());
    }

    @Override
    public String getName()
    {
        return id;
    }

    @Override
    public String getUsage(ICommandSender sender)
    {
        return "/" + id;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        if (args.length == 0 || args[0].equalsIgnoreCase("help"))
        {
            displayHelp(sender, false);
        }
        else
        {
            final String subCommand = args[0].toLowerCase();
            if (subCommandMap.containsKey(subCommand))
            {
                subCommandMap.get(subCommand).execute(server, sender, removeFront(args));
            }
            else if (args[0].equalsIgnoreCase("remove"))
            {
                commandRemove(server, sender, args);
            }
            else if (args[0].equalsIgnoreCase("lag"))
            {
                commandLag(server, sender, args);
            }
            else if (args[0].equalsIgnoreCase("debug"))
            {
                commandLag(server, sender, args);
            }
            else
            {
                sender.sendMessage(new TextComponentString("\u00a7c" + "Unknown command! Use '" + getUsage(sender) + " help' for more a list of commands"));
            }
        }
    }

    protected String[] removeFront(String[] args)
    {
        if (args.length == 0 || args.length == 1)
        {
            return new String[0];
        }
        return Arrays.copyOfRange(args, 1, args.length);
    }

    protected void displayHelp(ICommandSender sender, boolean error)
    {
        if (sender instanceof EntityPlayer)
        {
            sender.sendMessage(new TextComponentString((error ? "\u00a7c" : "") + "/icbmc blast list"));
            sender.sendMessage(new TextComponentString((error ? "\u00a7c" : "") + "/icbmc blast <id> <scale>"));
            sender.sendMessage(new TextComponentString((error ? "\u00a7c" : "") + "/icbmc blast <id> <x> <y> <z> <scale>"));
            sender.sendMessage(new TextComponentString((error ? "\u00a7c" : "") + "/icbmc remove <all/missiles/explosions> [radius]"));
            sender.sendMessage(new TextComponentString((error ? "\u00a7c" : "") + "/icbmc remove <all/missiles/explosions> <dim> <x> <y> <z> <radius>"));
            sender.sendMessage(new TextComponentString((error ? "\u00a7c" : "") + "/icbmc lag [radius]"));
        }
        else
        {
            sender.sendMessage(new TextComponentString((error ? "\u00a7c" : "") + "/icbmc blast list"));
            sender.sendMessage(new TextComponentString((error ? "\u00a7c" : "") + "/icbmc blast <id> <dim> <x> <y> <z> <scale>"));
            sender.sendMessage(new TextComponentString((error ? "\u00a7c" : "") + "/icbmc remove <all/missiles/explosions> <dim> <x> <y> <z> <radius>"));
        }
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
                boolean hasRange = args.length == 3 || args.length == 7;
                int range = args.length == 3 ? parseRadius(args[2])
                        : args.length == 7 ? parseRadius(args[6])
                        : -1;

                //Get position
                World world;
                double x, y, z;

                if (args.length == 6)
                {
                    world = DimensionManager.getWorld(Integer.getInteger(args[2]));
                    x = Double.parseDouble(args[3]);
                    y = Double.parseDouble(args[4]);
                    z = Double.parseDouble(args[5]);
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
                    //Get entities
                    List<Entity> entities = getEntities(world, x, y, z, range);

                    int count = 0;

                    //Loop with for-loop to prevent CME
                    for (int i = 0; i < entities.size(); i++)
                    {
                        Entity entity = entities.get(i);
                        if (entity != null && !entity.isDead)
                        {
                            boolean isExplosive = entity instanceof EntityExplosive;
                            boolean isMissile = isMissile(entity);
                            boolean isICBM = isICBMEntity(entity);
                            if (remove_explosives && isExplosive || remove_missiles && isMissile || remove_all && isICBM)
                            {
                                entity.setDead();
                                count++;
                            }
                        }
                    }

                    sender.sendMessage(new TextComponentString("Removed '" + count + "' ICBM entities " + (hasRange ? "within " + range + "meters" : "from the world")));
                    return; //Necessary return
                }
                else
                {
                    throw new WrongUsageException("Failed to get a world instance from arguments or sender.");
                }
            }

        }
        throw new WrongUsageException("'/icbmc remove <all/missile/explosion> [radius]' or '/icbmc remove <all/missile/explosion> <x> <y> <z> <radius>'");
    }

    protected void commandLag(MinecraftServer server, ICommandSender sender, String[] args) throws WrongUsageException
    {
        //Parse range
        double range = args.length > 1 ? Double.parseDouble(args[1]) : 1000;

        //Get entities
        List<Entity> entities = getEntities(sender.getEntityWorld(), sender.getPositionVector().x, sender.getPositionVector().y, sender.getPositionVector().z, range);

        int count = 0;
        //Loop with for-loop to prevent CME
        for (int i = 0; i < entities.size(); i++)
        {
            Entity entity = entities.get(i);
            if (entity != null && !entity.isDead)
            {
                if (isICBMEntity(entity))
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

    private boolean isICBMEntity(Entity entity)
    {
        return entity instanceof EntityFragments || entity instanceof EntityFlyingBlock || isMissile(entity) || entity instanceof EntityExplosive;
    }

    private boolean isMissile(Entity entity)
    {
        return entity instanceof EntityMissile;
    }

    protected List<Entity> getEntities(World world, double x, double y, double z, double range)
    {
        if (range > 0)
        {
            AxisAlignedBB bb = new AxisAlignedBB(
                    x - range, y - range, z - range,
                    x + range, y + range, z + range);

            return world.getEntitiesWithinAABB(Entity.class, bb);
        }
        else if (range == -1)
        {
            return world.loadedEntityList;
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
