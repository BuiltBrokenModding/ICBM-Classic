package icbm.classic;

import icbm.classic.content.entity.EntityExplosion;
import icbm.classic.content.entity.EntityExplosive;
import icbm.classic.content.entity.EntityFlyingBlock;
import icbm.classic.content.entity.EntityFragments;
import icbm.classic.content.entity.missile.EntityMissile;
import icbm.classic.content.explosive.blast.BlastEMP;
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

	private static final String[] EMP_MESSAGES = new String[]{
		"Did you pay the power bill?",
		"See them power their toys now!",
		"Hey who turned the lights out.",
		"Ha! I run on steam power!",
		"The power of lighting at my finger tips!"};

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
		EntityPlayer player = (EntityPlayer) sender;

		if (args.length == 0)
		{
			player.sendMessage(new TextComponentString("\u00a7c" + "ICBM Classic (" + (ICBMClassic.VERSION.startsWith("@") ? "DEV" : ICBMClassic.VERSION) + ")"));
			player.sendMessage(new TextComponentString(""));
			player.sendMessage(new TextComponentString("\u00a7c" + "/icbmc emp <radius>"));
			player.sendMessage(new TextComponentString("\u00a7c" + "/icbmc remove <all/missile/explosion> {radius}"));
		} else if (args[0].equalsIgnoreCase("remove"))
		{
			if (args.length == 2 || args.length == 3)
			{
				boolean all = args[1].equalsIgnoreCase("all");
				boolean missile = all || args[1].equalsIgnoreCase("missile");
				boolean explosion = all || args[1].equalsIgnoreCase("explosion");
				String str = all ? "entities" : missile ? "missiles" : explosion ? "explosions" : null;

				if (str != null)
				{
					boolean ranged = args.length == 3;
					int r = ranged ? parseRadius(args[2]) : -1;
					List<Entity> entities = ranged ? player.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(player.posX - r, player.posY - r, player.posZ - r, player.posX + r, player.posY + r, player.posZ + r))
						                        : new ArrayList<>(player.world.loadedEntityList); //Must clone the backing list to avoid CME

					for (Entity entity : entities)
					{
						if (explosion && entity instanceof EntityFlyingBlock)
						{
							((EntityFlyingBlock) entity).setBlock();
						} else if ((missile && entity instanceof EntityMissile) || (explosion && (entity instanceof EntityExplosive || entity instanceof EntityExplosion || entity instanceof EntityFragments)))
						{
							entity.setDead();
						}
					}

					player.sendMessage(new TextComponentString("Removed all ICBM " + str + " " + (ranged ? "within " + r + " blocks" : "in this world")));
					return; //Necessary return
				}

			}
			throw new WrongUsageException("/icbmc remove <all/missile/explosion> {radius}");
		} else if (args[0].equalsIgnoreCase("emp"))
		{
			int radius = parseRadius(args[1]);
			new BlastEMP(player.world, null, player.posX, player.posY, player.posZ, radius).setEffectBlocks().setEffectEntities().doExplode();

			String message = player.world.rand.nextFloat() < 0.25 ? //Chance of special message
				                 EMP_MESSAGES[player.world.rand.nextInt(EMP_MESSAGES.length)]
				                 : "Zap!";
			player.sendMessage(new TextComponentString(message));
		} else
		{
			player.sendMessage(new TextComponentString("\u00a7c" + "Unknown ICBM command! Use /icbmc to see a list of commands."));
		}
	}

	private static int parseRadius(String input) throws WrongUsageException
	{
		try
		{
			int radius = Integer.parseInt(input);
			if (radius <= 0)
			{
				throw new WrongUsageException("Radius must be greater than zero!");
			}
			return radius;
		} catch (NumberFormatException e)
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
