package icbm.classic.command;

import icbm.classic.content.entity.EntityExplosion;
import icbm.classic.content.entity.EntityExplosive;
import icbm.classic.content.entity.EntityFlyingBlock;
import icbm.classic.content.entity.EntityFragments;
import icbm.classic.content.entity.EntityGrenade;
import icbm.classic.content.entity.missile.EntityMissile;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Created by Robert Seifert on 1/2/20.
 */
public class CommandUtils
{

    /**
     * Removes the first entry in the array and returns a sub array
     *
     * @param args - array
     * @return sub array or empty array
     */
    public static String[] removeFront(String[] args)
    {
        if (args.length == 0 || args.length == 1)
        {
            return new String[0];
        }
        return Arrays.copyOfRange(args, 1, args.length);
    }

    /**
     * Checks if the entity is an ICBM entity
     *
     * @param entity - entity to check
     * @return true if the entity is from the mod ICBM
     */
    public static boolean isICBMEntity(Entity entity)
    {
        return entity instanceof EntityFragments
                || entity instanceof EntityFlyingBlock
                || isMissile(entity)
                || entity instanceof EntityExplosive
                || entity instanceof EntityExplosion
                || entity instanceof EntityGrenade;
    }

    /**
     * Checks if the entity is a missile
     *
     * @param entity - entity to check
     * @return true if the entity is a missile
     */
    public static boolean isMissile(Entity entity)
    {
        return entity instanceof EntityMissile;
    }

    /**
     * Gets all entities within the world and range
     *
     * @param world - world to check
     * @param x     - position
     * @param y     - position
     * @param z     - position
     * @param range - range to check, -1 will return all entities in the world
     * @return entities found
     */
    public static List<Entity> getEntities(World world, double x, double y, double z, double range, Predicate<Entity> filter)
    {
        if (range > 0)
        {
            AxisAlignedBB bb = new AxisAlignedBB(
                    x - range, y - range, z - range,
                    x + range, y + range, z + range);

            return world.getEntitiesWithinAABB(Entity.class, bb, filter::test);
        }
        //Copy list to avoid modification while we are using said list
        return world.loadedEntityList.stream().filter(filter).collect(Collectors.toList());
    }

    /**
     * Parses the radius string
     *
     * @param input - string input from the user
     * @return numeric radius
     * @throws WrongUsageException
     */
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

    /**
     * Gets a numeric value from the user while allowing for tilde offsets
     *
     * @param sender - command sender
     * @param value  - string input from user
     * @param alt    - numeric offset, normally position of command sender
     * @return numeric value
     * @throws WrongUsageException - if ~ is used from the server console
     */
    public static double getNumber(ICommandSender sender, String value, double alt) throws WrongUsageException
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
                return alt + Double.parseDouble(value.substring(1));
            }
            throw new WrongUsageException("'~' can't be used from console");
        }
        else
        {
            return Double.parseDouble(value);
        }
    }

    /**
     * Gets the world from user input
     *
     * @param sender - user running the command
     * @param value  - user inputted dimension ID
     * @param alt    - value to use for ~
     * @return world if found
     * @throws WrongUsageException - if input is invalid or world was not found
     */
    public static World getWorld(ICommandSender sender, String value, World alt) throws WrongUsageException
    {
        if (value.equals("~"))
        {
            if (!(sender instanceof MinecraftServer))
            {
                return alt;
            }
            throw new WrongUsageException("'~' can't be used from console");
        }
        try
        {
            //Parse dim ID from user input
            final int dim = Integer.parseInt(value);

            //Get world using ID
            final World world = DimensionManager.getWorld(dim);
            if (world == null)
            {
                throw new WrongUsageException("Dimension with ID[" + value + "] was not found!");
            }
            return world;
        } catch (NumberFormatException e)
        {
            throw new WrongUsageException("Invalid dimension ID[" + value + "]!");
        }
    }
}
