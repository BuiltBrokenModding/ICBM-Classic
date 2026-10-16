package icbm.classic.command;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.content.blast.redmatter.EntityRedmatter;
import icbm.classic.content.cluster.bomblet.EntityBombDroplet;
import icbm.classic.content.entity.EntityExplosion;
import icbm.classic.content.entity.EntityExplosive;
import icbm.classic.content.entity.EntityFragments;
import icbm.classic.content.entity.EntityGrenade;
import icbm.classic.content.entity.flyingblock.EntityFlyingBlock;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

/**
 * Created by Robin Seifert on 1/2/20.
 */
public class CommandUtils
{
    /** Name {@link MinecraftServer#createCommandSourceStack()} gives the server console */
    private static final String CONSOLE_NAME = "Server";

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
     * Replaces 1.12 {@code sender instanceof MinecraftServer}.
     * <p>
     * PORT: 1.21 command sources don't expose the underlying {@code CommandSource}. The server console is the
     * only source without an entity that is named "Server" (players always have an entity, command blocks
     * default to "@", rcon is "Rcon").
     *
     * @param sender - source running the command
     * @return true if the command is run from the server console
     */
    public static boolean isConsole(CommandSourceStack sender)
    {
        return sender.getEntity() == null && CONSOLE_NAME.equals(sender.getTextName());
    }

    /**
     * Creates the equivalent of a 1.12 {@code WrongUsageException}/{@code SyntaxErrorException}
     *
     * @param translationKey - translation key, or plain text (1.12 showed untranslatable keys as-is)
     * @param args           - translation arguments
     * @return exception to throw
     */
    public static CommandSyntaxException error(String translationKey, Object... args)
    {
        return new SimpleCommandExceptionType(Component.translatable(translationKey, args)).create();
    }

    /**
     * Creates an exception showing plain text
     *
     * @param message - message to show
     * @return exception to throw
     */
    public static CommandSyntaxException errorLiteral(String message)
    {
        return new SimpleCommandExceptionType(Component.literal(message)).create();
    }

    /**
     * Replaces 1.12 {@code CommandBase#parseInt}
     */
    public static int parseInt(String input) throws CommandSyntaxException
    {
        try
        {
            return Integer.parseInt(input);
        }
        catch (NumberFormatException e)
        {
            throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.readerInvalidInt().create(input);
        }
    }

    /**
     * Replaces 1.12 {@code CommandBase#parseInt(String, int)}
     */
    public static int parseInt(String input, int min) throws CommandSyntaxException
    {
        final int value = parseInt(input);
        if (value < min)
        {
            throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.integerTooLow().create(value, min);
        }
        return value;
    }

    /**
     * Parses a float, 1.12 let {@link NumberFormatException} through as a generic command error
     */
    public static float parseFloat(String input) throws CommandSyntaxException
    {
        try
        {
            return Float.parseFloat(input);
        }
        catch (NumberFormatException e)
        {
            throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.readerInvalidFloat().create(input);
        }
    }

    /**
     * Parses a double, 1.12 let {@link NumberFormatException} through as a generic command error
     */
    public static double parseDouble(String input) throws CommandSyntaxException
    {
        try
        {
            return Double.parseDouble(input);
        }
        catch (NumberFormatException e)
        {
            throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.readerInvalidDouble().create(input);
        }
    }

    /**
     * Replaces 1.12 {@code CommandBase#getListOfStringsMatchingLastWord}
     *
     * @param args     - arguments, last is the word being typed
     * @param possible - options
     * @return options starting with the last word (case insensitive)
     */
    public static List<String> getListOfStringsMatchingLastWord(String[] args, Collection<String> possible)
    {
        final String last = args.length == 0 ? "" : args[args.length - 1].toLowerCase();
        final List<String> list = new ArrayList<>();
        for (String option : possible)
        {
            if (option.toLowerCase().startsWith(last))
            {
                list.add(option);
            }
            else
            {
                // 1.12 also matched resource locations by path only
                final ResourceLocation location = ResourceLocation.tryParse(option);
                if (location != null && location.getPath().toLowerCase().startsWith(last))
                {
                    list.add(option);
                }
            }
        }
        return list;
    }

    public static List<String> getListOfStringsMatchingLastWord(String[] args, String... possible)
    {
        return getListOfStringsMatchingLastWord(args, Arrays.asList(possible));
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
                || entity instanceof EntityBombDroplet
                || entity instanceof EntityExplosive
                || entity instanceof EntityExplosion
                || entity instanceof EntityGrenade
                || entity instanceof EntityRedmatter; //TODO just check for ICBM namespace
    }

    /**
     * Checks if the entity is a missile
     *
     * @param entity - entity to check
     * @return true if the entity is a missile
     */
    public static boolean isMissile(Entity entity)
    {
        return entity.getCapability(ICBMClassicAPI.MISSILE_CAPABILITY) != null;
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
    public static List<Entity> getEntities(Level world, double x, double y, double z, double range, Predicate<Entity> filter)
    {
        if (range > 0)
        {
            AABB bb = new AABB(
                    x - range, y - range, z - range,
                    x + range, y + range, z + range);

            return world.getEntitiesOfClass(Entity.class, bb, filter);
        }
        //Copy list to avoid modification while we are using said list
        final List<Entity> list = new ArrayList<>();
        if (world instanceof ServerLevel serverLevel)
        {
            for (Entity entity : serverLevel.getAllEntities())
            {
                if (entity != null && filter.test(entity))
                {
                    list.add(entity);
                }
            }
        }
        return list;
    }

    /**
     * Parses the radius string
     *
     * @param input - string input from the user
     * @return numeric radius
     * @throws CommandSyntaxException
     */
    public static int parseRadius(String input) throws CommandSyntaxException
    {
        try
        {
            int radius = Integer.parseInt(input);
            if (radius <= 0)
            {
                throw errorLiteral("Radius must be greater than zero!");
            }
            return radius;
        } catch (NumberFormatException e)
        {
            throw errorLiteral("Invalid radius!");
        }
    }

    /**
     * Gets a numeric value from the user while allowing for tilde offsets
     *
     * @param sender - command sender
     * @param value  - string input from user
     * @param alt    - numeric offset, normally position of command sender
     * @return numeric value
     * @throws CommandSyntaxException - if ~ is used from the server console
     */
    public static double getNumber(CommandSourceStack sender, String value, double alt) throws CommandSyntaxException
    {
        if (value.equals("~"))
        {
            if (!isConsole(sender))
            {
                return alt;
            }
            throw errorLiteral("'~' can't be used from console");
        }
        else if (value.startsWith("~"))
        {
            if (!isConsole(sender))
            {
                return alt + parseDouble(value.substring(1));
            }
            throw errorLiteral("'~' can't be used from console");
        }
        else
        {
            return parseDouble(value);
        }
    }

    /**
     * Gets the world from user input
     * <p>
     * PORT: 1.12 took an integer dimension id. 1.21 takes the dimension's registry name
     * (e.g. {@code minecraft:the_nether}). The vanilla ids 0, -1 and 1 are still accepted for overworld, nether and end.
     *
     * @param sender - user running the command
     * @param value  - user inputted dimension ID
     * @param alt    - value to use for ~
     * @return world if found
     * @throws CommandSyntaxException - if input is invalid or world was not found
     */
    public static Level getWorld(CommandSourceStack sender, String value, Level alt) throws CommandSyntaxException
    {
        if (value.equals("~"))
        {
            if (!isConsole(sender))
            {
                return alt;
            }
            throw errorLiteral("'~' can't be used from console");
        }

        //Parse dim ID from user input
        final ResourceKey<Level> dim = parseDimension(value);
        if (dim == null)
        {
            throw errorLiteral("Invalid dimension ID[" + value + "]!");
        }

        //Get world using ID
        final Level world = sender.getServer().getLevel(dim);
        if (world == null)
        {
            throw errorLiteral("Dimension with ID[" + value + "] was not found!");
        }
        return world;
    }

    private static ResourceKey<Level> parseDimension(String value)
    {
        switch (value)
        {
            case "0":
                return Level.OVERWORLD;
            case "-1":
                return Level.NETHER;
            case "1":
                return Level.END;
            default:
                final ResourceLocation location = ResourceLocation.tryParse(value);
                return location != null ? ResourceKey.create(Registries.DIMENSION, location) : null;
        }
    }

    /**
     * @return dimension names for tab completion
     */
    public static List<String> getDimensionNames(MinecraftServer server)
    {
        final List<String> list = new ArrayList<>();
        if (server != null)
        {
            server.levelKeys().forEach(key -> list.add(key.location().toString()));
        }
        list.add("~");
        return list;
    }
}
// PORTED
