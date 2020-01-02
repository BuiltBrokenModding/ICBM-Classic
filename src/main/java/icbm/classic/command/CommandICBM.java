package icbm.classic.command;

import icbm.classic.command.imp.SubCommand;
import icbm.classic.command.sub.CommandBlast;
import icbm.classic.command.sub.CommandLag;
import icbm.classic.command.sub.CommandRemove;
import icbm.classic.content.entity.EntityExplosive;
import icbm.classic.content.entity.EntityFlyingBlock;
import icbm.classic.content.entity.EntityFragments;
import icbm.classic.content.entity.missile.EntityMissile;
import icbm.classic.lib.MapWithDefault;
import icbm.classic.lib.explosive.ExplosiveHandler;
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

    private final MapWithDefault<String, SubCommand> subCommandMap = new MapWithDefault();
    private final String id;

    public CommandICBM(String id)
    {
        this.id = id;

        //Sub commands
        subCommandMap.put("blast", new CommandBlast(this));
        subCommandMap.put("remove", new CommandRemove(this));
        subCommandMap.put("lag", new CommandLag(this));

        //Help command
        SubCommand helpCommand = new SubCommand(this, "help")
        {
            @Override
            public void execute(MinecraftServer server, ICommandSender sender, String[] args)
            {
                subCommandMap.values().forEach(command -> command.displayHelp(sender));
            }
        };
        subCommandMap.put("help", helpCommand);
        subCommandMap.put("?", helpCommand);
        subCommandMap.setDefaultValue(helpCommand);
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
        final String subCommand = args.length == 0 ? "help" : args[0].toLowerCase();
        subCommandMap.get(subCommand).execute(server, sender, CommandUtils.removeFront(args));
    }

    @Override
    public int getRequiredPermissionLevel()
    {
        return 2;
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos)
    {
        if (args.length == 1)
        {
            return getListOfStringsMatchingLastWord(args, subCommandMap.keySet());
        }
        else if (args.length == 2)
        {
            final String subCommand = args.length == 0 ? "help" : args[0].toLowerCase();
            subCommandMap.get(subCommand).getTabCompletions(server, sender, CommandUtils.removeFront(args), targetPos);
        }
        return new ArrayList<>();
    }

    public static boolean isICBMEntity(Entity entity)
    {
        return entity instanceof EntityFragments || entity instanceof EntityFlyingBlock || isMissile(entity) || entity instanceof EntityExplosive;
    }

    public static boolean isMissile(Entity entity)
    {
        return entity instanceof EntityMissile;
    }

    public static List<Entity> getEntities(World world, double x, double y, double z, double range)
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

    public static int parseRadius(String input) throws WrongUsageException
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
}
