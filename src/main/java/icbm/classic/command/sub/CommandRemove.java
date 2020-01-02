package icbm.classic.command.sub;

import icbm.classic.command.CommandUtils;
import icbm.classic.command.imp.SubCommand;
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
import net.minecraftforge.common.DimensionManager;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Created by Dark(DarkGuardsman, Robert) on 4/13/2018.
 */
public class CommandRemove extends SubCommand
{

    public CommandRemove(CommandBase parent)
    {
        super(parent, "remove");
    }

    @Override
    protected void collectHelpServer(Consumer<String> consumer)
    {
        consumer.accept("<all/missiles/explosions> <dim> <x> <y> <z> <radius>");
    }

    @Override
    protected void collectHelpPlayer(Consumer<String> consumer)
    {
        consumer.accept("<all/missiles/explosions> [radius]");
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
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
                int range = args.length == 3 ? CommandUtils.parseRadius(args[2])
                        : args.length == 7 ? CommandUtils.parseRadius(args[6])
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
                    List<Entity> entities = CommandUtils.getEntities(world, x, y, z, range);

                    int count = 0;

                    //Loop with for-loop to prevent CME
                    for (int i = 0; i < entities.size(); i++)
                    {
                        Entity entity = entities.get(i);
                        if (entity != null && !entity.isDead)
                        {
                            boolean isExplosive = entity instanceof EntityExplosive;
                            boolean isMissile = CommandUtils.isMissile(entity);
                            boolean isICBM = CommandUtils.isICBMEntity(entity);
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

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos)
    {
        return args.length == 1 ? getListOfStringsMatchingLastWord(args, "all", "missile", "explosion") : new ArrayList<>();
    }
}
