package icbm.classic.command.sub;

import icbm.classic.command.CommandUtils;
import icbm.classic.command.system.SubCommand;
import icbm.classic.content.entity.EntityExplosive;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Created by Dark(DarkGuardsman, Robert) on 4/13/2018.
 */
public class CommandRemove extends SubCommand
{
    public CommandRemove()
    {
        super("remove");
    }

    @Override
    protected void collectHelpForAll(Consumer<String> consumer)
    {
        consumer.accept("<all/missiles/explosions> <dim> <x> <y> <z> <radius>");
    }

    @Override
    protected void collectHelpWorldOnly(Consumer<String> consumer)
    {
        consumer.accept("<all/missiles/explosions> [radius]");
    }

    @Override
    public void handleCommand(@Nonnull MinecraftServer server, @Nonnull ICommandSender sender, @Nonnull String[] args) throws CommandException
    {
        if (args.length <= 0 || !doCommand(sender, args))
        {
            throw new WrongUsageException("'/icbmc remove <all/missile/explosion> [radius]' or '/icbmc remove <all/missile/explosion> <x> <y> <z> <radius>'");
        }
    }

    private boolean doCommand(@Nonnull ICommandSender sender, @Nonnull String[] args) throws CommandException
    {
        //Get type
        final Predicate<Entity> entitySelector = buildSelector(args[0]);

        //Get range
        final int range = args.length == 2 ? CommandUtils.parseRadius(args[1])
                : args.length == 6 ? CommandUtils.parseRadius(args[5])
                : -1;

        //Get position
        World world;
        double x, y, z;

        if (args.length == 6)
        {
            world = CommandUtils.getWorld(sender, args[1], sender.getEntityWorld());
            x = Double.parseDouble(args[2]);
            y = Double.parseDouble(args[3]);
            z = Double.parseDouble(args[4]);
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
            return false;
        }

        //Find and set entities dead
        final List<Entity> entities = CommandUtils.getEntities(world, x, y, z, range, entitySelector);
        entities.forEach(Entity::setDead);

        //User feedback
        sender.sendMessage(new TextComponentString("Removed '" + entities.size() + "' ICBM entities " + (range > 0 ? "within " + range + "meters" : "from the world"))); //TODO translate

        return true;
    }

    private boolean isRemoveMissile(String type)
    {
        return type.equalsIgnoreCase("missiles") || type.equalsIgnoreCase("missile");
    }

    private boolean isRemoveExplosion(String type)
    {
        return type.equalsIgnoreCase("explosions")
                || type.equalsIgnoreCase("explosion")
                || type.equalsIgnoreCase("explosive")
                || type.equalsIgnoreCase("explosives")
                || type.equalsIgnoreCase("ex");
    }

    private Predicate<Entity> buildSelector(String type)
    {
        final boolean remove_all = type.equalsIgnoreCase("all");
        final boolean remove_missiles = isRemoveMissile(type);
        final boolean remove_explosives = isRemoveExplosion(type);

        return (entity) -> {
            if(entity.isEntityAlive())
            {
                if (remove_all)
                {
                    return CommandUtils.isICBMEntity(entity);
                }
                else if (remove_explosives)
                {
                    return entity instanceof EntityExplosive;
                }
                return remove_missiles && CommandUtils.isMissile(entity);
            }
            return false;
        };
    }

    @Override
    public List<String> getTabSuggestions(@Nonnull MinecraftServer server, @Nonnull ICommandSender sender, @Nonnull String[] args, @Nullable BlockPos targetPos)
    {
        return args.length == 1 ? CommandBase.getListOfStringsMatchingLastWord(args, "all", "missile", "explosion") : new ArrayList<>();
    }
}
