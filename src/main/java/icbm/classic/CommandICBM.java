package icbm.classic;

import cpw.mods.fml.common.Loader;
import icbm.classic.content.entity.*;
import net.minecraft.command.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CommandICBM extends CommandBase
{

	private String name;

    @Override
    public String getCommandName()
    {
    	if(name == null){
		    name = Loader.isModLoaded("icbm") ? "icbmc" : "icbm";
	    }
        return name;
    }

    @Override
    public String getCommandUsage(ICommandSender par1ICommandSender)
    {
        return "/icbmc help";
    }


	@Override
	public void processCommand(ICommandSender sender, String[] args) throws CommandException
	{
		if (args.length == 0 || args[0].equalsIgnoreCase("help"))
		{
			displayHelp(sender, false);
		}
		else
		{
			final String subCommand = args[0].toLowerCase();
			if (subCommand.equalsIgnoreCase("clear"))
			{
				commandClear(sender, args);
			}
			else if (subCommand.equalsIgnoreCase("remove"))
			{
				commandRemove(sender, args);
			}
			else if (subCommand.equalsIgnoreCase("lag"))
			{
				commandLag(sender, args);
			}
			else
			{
				sender.addChatMessage(new ChatComponentText("\u00a7c" + "Unknown command! Use '/" + getCommandName() + " help' for more a list of commands"));
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

	private void displayHelp(ICommandSender sender, boolean error)
	{
		if (sender instanceof EntityPlayer)
		{
			sender.addChatMessage(new ChatComponentText((error ? "\u00a7c" : "") + "/" + name + " clear"));
			sender.addChatMessage(new ChatComponentText((error ? "\u00a7c" : "") + "/" + name + " lag [radius]"));
			sender.addChatMessage(new ChatComponentText((error ? "\u00a7c" : "") + "/" + name + " remove <all/missiles/explosions> [radius]"));
			sender.addChatMessage(new ChatComponentText((error ? "\u00a7c" : "") + "/" + name + " remove <all/missiles/explosions> <dim> <x> <y> <z> <radius>"));
		}
		else
		{
			sender.addChatMessage(new ChatComponentText((error ? "\u00a7c" : "") + "/" + name + " clear"));
			sender.addChatMessage(new ChatComponentText((error ? "\u00a7c" : "") + "/" + name + " remove <all/missiles/explosions> <dim> <x> <y> <z> <radius>"));
		}
	}

	private void commandClear(ICommandSender sender, String[] args)
	{
		int count = 0;

		for(WorldServer world : DimensionManager.getWorlds())
		{
		    //Get entities
		    List<Entity> entities = world.loadedEntityList;

		    //Loop with for-loop to prevent CME
		    for (int i = 0; i < entities.size(); i++)
		    {
			    Entity entity = entities.get(i);
			    if (entity != null && !entity.isDead && isICBMEntity(entity))
			    {
					    entity.setDead();
					    count++;
			    }
		    }
	    }
		sender.addChatMessage(new ChatComponentText("Removed '" + count + "' ICBM entities from all worlds"));
	}

	private void commandRemove(ICommandSender sender, String[] args) throws WrongUsageException
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
					x = sender.getPlayerCoordinates().posX;
					y = sender.getPlayerCoordinates().posY;
					z = sender.getPlayerCoordinates().posZ;
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

					sender.addChatMessage(new ChatComponentText("Removed '" + count + "' ICBM entities " + (hasRange ? "within " + range + "meters" : "from the world")));
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

	private void commandLag(ICommandSender sender, String[] args) throws WrongUsageException
	{
		//Parse range
		double range = args.length > 1 ? Double.parseDouble(args[1]) : 1000;

		//Get entities
		List<Entity> entities = getEntities(sender.getEntityWorld(), sender.getPlayerCoordinates().posX, sender.getPlayerCoordinates().posY, sender.getPlayerCoordinates().posZ, range);

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

		//Update user with data
		sender.addChatMessage(new ChatComponentText("Removed '" + count + "' ICBM entities within " + range + " meters"));
	}

	private boolean isICBMEntity(Entity entity)
	{
		return isMissile(entity)
			       ||entity instanceof EntityFragments
			       || entity instanceof EntityFlyingBlock
			       || entity instanceof EntityExplosive
			       || entity instanceof EntityGrenade;
	}

	private boolean isMissile(Entity entity)
	{
		return entity instanceof EntityMissile;
	}

	private List<Entity> getEntities(World world, double x, double y, double z, double range)
	{
		if (range > 0)
		{
			AxisAlignedBB bb = AxisAlignedBB.getBoundingBox(
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
	public List<String> addTabCompletionOptions(ICommandSender sender, String[] args)
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
