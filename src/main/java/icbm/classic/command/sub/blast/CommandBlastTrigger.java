package icbm.classic.command.sub.blast;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.ICBMClassicHelpers;
import icbm.classic.api.actions.data.ActionFields;
import icbm.classic.api.actions.status.IActionStatus;
import icbm.classic.api.reg.IExplosiveData;
import icbm.classic.command.CommandUtils;
import icbm.classic.command.ICBMCommands;
import icbm.classic.command.system.SubCommand;
import icbm.classic.content.missile.logic.source.ActionSource;
import icbm.classic.lib.actions.fields.ActionFieldProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Created by Robin Seifert on 1/6/20.
 */
public class CommandBlastTrigger extends SubCommand
{
    //Translations
    private static final String TRANSLATION_KEY = "command.icbmclassic:icbm.blast";
    public static final String TRANSLATION_TRIGGERED = TRANSLATION_KEY + ".triggered";
    public static final String TRANSLATION_THREADING = TRANSLATION_KEY + ".threading";

    //Translations: Errors
    public static final String TRANSLATION_ERROR = TRANSLATION_KEY + ".error";
    public static final String TRANSLATION_ERROR_BLOCKED = TRANSLATION_ERROR + ".blocked";
    public static final String TRANSLATION_ERROR_NULL = TRANSLATION_ERROR + ".null";
    public static final String TRANSLATION_ERROR_TRIGGERED = TRANSLATION_ERROR + ".triggered";
    public static final String TRANSLATION_ERROR_UNKNOWN = TRANSLATION_ERROR + ".unknown";

    public static final String TRANSLATION_ERROR_SCALE_ZERO = TRANSLATION_ERROR + ".scale.zero";
    public static final String TRANSLATION_ERROR_EXPLOSIVE_ID = TRANSLATION_ERROR + ".explosive.id";

    public CommandBlastTrigger()
    {
        super("trigger");
    }

    @Override
    protected void collectHelpForAll(Consumer<String> consumer)
    {
        consumer.accept("<id> <dim> <x> <y> <z> <size>");
    }

    @Override
    protected void collectHelpWorldOnly(Consumer<String> consumer)
    {
        consumer.accept("<id> <size>");
    }

    @Override
    public void handleCommand(@Nonnull MinecraftServer server, @Nonnull CommandSourceStack sender, @Nonnull String[] args) throws CommandSyntaxException
    {
        if (args.length <= 0 || !doCommand(server, sender, args))
        {
            throw CommandUtils.error(ICBMCommands.TRANSLATION_UNKNOWN_COMMAND, getUsage(sender));
        }
    }

    private boolean doCommand(@Nonnull MinecraftServer server, @Nonnull CommandSourceStack sender, @Nonnull String[] args) throws CommandSyntaxException
    {
        //Get explosive from user
        final IExplosiveData explosiveData = getExplosive(args[0]);

        if (args.length == 6)
        {
            longVersion(sender, explosiveData, args);
            return true;
        }
        else if (!CommandUtils.isConsole(sender) && args.length == 2)
        {
            shortVersion(sender, explosiveData, args);
            return true;
        }
        return false;
    }

    private void shortVersion(CommandSourceStack sender, IExplosiveData explosiveData, String[] args) throws CommandSyntaxException
    {
        final float scale = CommandUtils.parseFloat(args[1]);
        if (scale <= 0)
        {
            throw CommandUtils.error(TRANSLATION_ERROR_SCALE_ZERO);
        }

        //Get position data
        final Level world = sender.getLevel();
        final double x = sender.getPosition().x;
        final double y = sender.getPosition().y;
        final double z = sender.getPosition().z;

        //Trigger blast
        trigger(sender, world, x, y, z, explosiveData, scale);
    }

    private void longVersion(CommandSourceStack sender, IExplosiveData explosiveData, String[] args) throws CommandSyntaxException
    {
        final float size = CommandUtils.parseFloat(args[5]);
        if (size <= 0)
        {
            throw CommandUtils.error(TRANSLATION_ERROR_SCALE_ZERO);
        }

        //Get position data
        final Level world = CommandUtils.getWorld(sender, args[1], sender.getLevel());
        final double x = CommandUtils.getNumber(sender, args[2], sender.getPosition().x);
        final double y = CommandUtils.getNumber(sender, args[3], sender.getPosition().y);
        final double z = CommandUtils.getNumber(sender, args[4], sender.getPosition().z);

        //Trigger blast
        trigger(sender, world, x, y, z, explosiveData, size);
    }

    /**
     * Gets the explosive data based on the string ID from the user
     *
     * @param explosive_id - ID from user
     * @return explosive data
     * @throws CommandSyntaxException - if ID is not found
     */
    public static IExplosiveData getExplosive(String explosive_id) throws CommandSyntaxException
    {
        // PORT: ResourceLocation.parse throws on invalid characters in 1.21, treat as not found
        final IExplosiveData explosiveData = ResourceLocation.tryParse(explosive_id) != null ? ICBMClassicHelpers.getExplosive(explosive_id, true) : null;
        if (explosiveData == null)
        {
            throw CommandUtils.error(TRANSLATION_ERROR_EXPLOSIVE_ID, explosive_id);
        }
        return explosiveData;
    }

    /**
     * @return explosive names for tab completion
     */
    public static List<String> getExplosiveNames()
    {
        if (ICBMClassicAPI.EXPLOSIVE_REGISTRY == null || ICBMClassicAPI.EXPLOSIVE_REGISTRY.getExplosives() == null)
        {
            return Collections.emptyList();
        }
        return ICBMClassicAPI.EXPLOSIVE_REGISTRY.getExplosives().stream()
                .map(IExplosiveData::getRegistryKey)
                .map(ResourceLocation::toString)
                .sorted()
                .collect(Collectors.toList());
    }

    /**
     * Triggers the explosive at the location
     *
     * @param sender        - user running the command
     * @param world         - position data
     * @param x             - position data
     * @param y             - position data
     * @param z             - position data
     * @param explosiveData - explosive to run
     * @param size          - size to set
     */
    private void trigger(CommandSourceStack sender, Level world, double x, double y, double z, IExplosiveData explosiveData, float size)
    {
        final IActionStatus result = explosiveData.create(world, x, y, z,  new ActionSource(),new ActionFieldProvider().field(ActionFields.AREA_SIZE, () -> size)).doAction();

        //Send translated message to user
        // PORT: 1.12 sent getTooltip(), which resolves to an untranslated `action.status.*` key. message() is the status text
        sender.sendSystemMessage(result.message());
    }

    @Override
    public List<String> getTabSuggestions(@Nonnull MinecraftServer server, @Nonnull CommandSourceStack sender, @Nonnull String[] args, @Nullable BlockPos targetPos)
    {
        // PORT: 1.12 had no suggestions here, added explosive ids and dimension names for Brigadier
        if (args.length == 1)
        {
            return CommandUtils.getListOfStringsMatchingLastWord(args, getExplosiveNames());
        }
        else if (args.length == 2)
        {
            return CommandUtils.getListOfStringsMatchingLastWord(args, CommandUtils.getDimensionNames(server));
        }
        return Collections.emptyList();
    }
}
// PORTED
