package icbm.classic.command.sub.blast;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
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
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

/**
 * Created by Robin Seifert on 1/6/20.
 */
public class CommandBlastSpread extends SubCommand
{
    private static final String TRANSLATION_KEY = "command.icbmclassic:icbm.spread";
    public static final String TRANSLATION_SPREAD_START = TRANSLATION_KEY + ".started";

    public CommandBlastSpread()
    {
        super("spread");
    }

    @Override
    protected void collectHelpForAll(Consumer<String> consumer)
    {
        consumer.accept("<count> <distance> <id> <dim> <x> <y> <z> <scale>");
    }

    @Override
    public void handleCommand(@Nonnull MinecraftServer server, @Nonnull CommandSourceStack sender, @Nonnull String[] args) throws CommandSyntaxException
    {
        if (args.length == 8)
        {
            doCommand(sender, args);
        }
        else
        {
            throw CommandUtils.error(ICBMCommands.TRANSLATION_UNKNOWN_COMMAND, getUsage(sender));
        }
    }

    private void doCommand(@Nonnull CommandSourceStack sender, @Nonnull String[] args) throws CommandSyntaxException
    {
        final int count = CommandUtils.parseInt(args[0], 1);
        final int distance = CommandUtils.parseInt(args[1], 1);

        //Get explosive data from user
        final IExplosiveData explosiveData = CommandBlastTrigger.getExplosive(args[2]);

        //Get world position
        final BlockPos senderPos = BlockPos.containing(sender.getPosition());
        final Level world = CommandUtils.getWorld(sender, args[3], sender.getLevel());
        final double xInput = CommandUtils.getNumber(sender, args[4], senderPos.getX() + 0.5);
        final double yInput = CommandUtils.getNumber(sender, args[5], senderPos.getY() + 0.5);
        final double zInput = CommandUtils.getNumber(sender, args[6], senderPos.getZ() + 0.5);

        //Get scale from user
        final float scale = CommandUtils.parseFloat(args[7]); //TODO turn into helper method
        if (scale <= 0)
        {
            throw CommandUtils.error(CommandBlastTrigger.TRANSLATION_ERROR_SCALE_ZERO);
        }

        final int expectedSpawnCount = (int)Math.floor(Math.pow((count * 2) + 1, 2));

        // PORT: 1.12 sent the int dimension id and world type name, 1.21 sends the dimension and dimension type names
        sender.sendSystemMessage(Component.translatable(TRANSLATION_SPREAD_START,
                explosiveData.getRegistryKey().toString(), scale,
                world.dimension().location().toString(),
                world.dimensionTypeRegistration().unwrapKey().map(key -> key.location().toString()).orElse("unknown"),
                xInput, yInput, zInput,
                count, distance,
                expectedSpawnCount));

        //Generate blasts in a grid
        for (int xi = -count; xi <= count; xi++)
        {
            for (int zi = -count; zi <= count; zi++)
            {
                //calc position
                final double x = xInput + xi * distance;
                final double z = zInput + zi * distance;

                //Trigger blast
                final IActionStatus result = explosiveData.create(world, x, yInput, z, new ActionSource(),new ActionFieldProvider().field(ActionFields.AREA_SIZE, () -> scale)).doAction();

                //Send translated message to user
                // PORT: 1.12 sent getTooltip(), which resolves to an untranslated `action.status.*` key. message() is the status text
                sender.sendSystemMessage(result.message());
            }
        }
    }

    @Override
    public List<String> getTabSuggestions(@Nonnull MinecraftServer server, @Nonnull CommandSourceStack sender, @Nonnull String[] args, @Nullable BlockPos targetPos)
    {
        // PORT: 1.12 had no suggestions here, added explosive ids and dimension names for Brigadier
        if (args.length == 3)
        {
            return CommandUtils.getListOfStringsMatchingLastWord(args, CommandBlastTrigger.getExplosiveNames());
        }
        else if (args.length == 4)
        {
            return CommandUtils.getListOfStringsMatchingLastWord(args, CommandUtils.getDimensionNames(server));
        }
        return Collections.emptyList();
    }
}
// PORTED
