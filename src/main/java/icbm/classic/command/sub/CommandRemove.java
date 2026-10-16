package icbm.classic.command.sub;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import icbm.classic.command.CommandUtils;
import icbm.classic.command.ICBMCommands;
import icbm.classic.command.system.SubCommand;
import icbm.classic.content.entity.EntityExplosive;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Created by Dark(DarkGuardsman, Robin) on 4/13/2018.
 */
public class CommandRemove extends SubCommand
{
    public static final String TRANSLATION_REMOVE = "command.icbmclassic:icbm.remove";

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
    public void handleCommand(@Nonnull MinecraftServer server, @Nonnull CommandSourceStack sender, @Nonnull String[] args) throws CommandSyntaxException
    {
        if (args.length <= 0 || !doCommand(sender, args))
        {
            throw CommandUtils.error(ICBMCommands.TRANSLATION_UNKNOWN_COMMAND, getUsage(sender));
        }
    }

    private boolean doCommand(@Nonnull CommandSourceStack sender, @Nonnull String[] args) throws CommandSyntaxException
    {
        //Get type
        final Predicate<Entity> entitySelector = buildSelector(args[0]);

        //Get range
        final int range = getRange(args);

        //Get position
        Level world;
        double x, y, z;

        //Long version, 5 args or 6 if range is applied
        if (args.length >= 5)
        {
            world = CommandUtils.getWorld(sender, args[1], sender.getLevel());
            x = CommandUtils.getNumber(sender, args[2], sender.getPosition().x);
            y = CommandUtils.getNumber(sender, args[3], sender.getPosition().y);
            z = CommandUtils.getNumber(sender, args[4], sender.getPosition().z);
        }
        //Short version, 1 arg or 2 if range is applied
        else if (!CommandUtils.isConsole(sender) && args.length <= 2)
        {
            world = sender.getLevel();
            x = sender.getPosition().x;
            y = sender.getPosition().y;
            z = sender.getPosition().z;
        }
        else
        {
            return false;
        }

        //Find and set entities dead
        final List<Entity> entities = CommandUtils.getEntities(world, x, y, z, range, entitySelector);
        entities.forEach(Entity::discard);

        //User feedback
        sender.sendSystemMessage(Component.translatable(TRANSLATION_REMOVE, entities.size(), range));

        return true;
    }

    private int getRange(String[] args) throws CommandSyntaxException
    {
        if(args.length == 2) {
            return CommandUtils.parseInt(args[1]);
        } else  if(args.length == 6) {
            return CommandUtils.parseInt(args[5]);
        }
        return -1;
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
            if(entity.isAlive())
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
    public List<String> getTabSuggestions(@Nonnull MinecraftServer server, @Nonnull CommandSourceStack sender, @Nonnull String[] args, @Nullable BlockPos targetPos)
    {
        return args.length == 1 ? CommandUtils.getListOfStringsMatchingLastWord(args, "all", "missile", "explosion") : new ArrayList<>();
    }
}
// PORTED
