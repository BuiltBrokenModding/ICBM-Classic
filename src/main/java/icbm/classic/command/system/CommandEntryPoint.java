package icbm.classic.command.system;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Entry point for all ICBM commands.
 * <p>
 * PORT: 1.12 extended {@code CommandBase}. In 1.21 this builds a Brigadier tree from the {@link ICommandGroup}:
 * every sub command becomes a literal node, and each node gets a greedy {@value #ARGS} argument that hands the
 * raw space separated arguments to {@link ISubCommand#handleCommand} exactly like 1.12 did. Unknown sub commands
 * fall through to the group's greedy argument, which shows help like the 1.12 default command did.
 */
public final class CommandEntryPoint
{
    /** Same as 1.12 {@code CommandEntryPoint#getRequiredPermissionLevel()} */
    public static final int PERMISSION_LEVEL = 2;

    /** Name of the greedy argument node holding raw arguments */
    public static final String ARGS = "args";

    private CommandEntryPoint()
    {
    }

    /**
     * Builds the root command node
     *
     * @param id           - name of the root command, without slash
     * @param commandGroup - group handling the command
     * @return builder to register with the dispatcher
     */
    public static LiteralArgumentBuilder<CommandSourceStack> build(String id, ICommandGroup commandGroup)
    {
        final LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal(id)
            .requires(source -> source.hasPermission(PERMISSION_LEVEL));
        return bridge(root, commandGroup);
    }

    private static LiteralArgumentBuilder<CommandSourceStack> bridge(LiteralArgumentBuilder<CommandSourceStack> node, ISubCommand command)
    {
        // No arguments
        node.executes(context -> run(context, command, new String[0]));

        // Literal per sub command, gives the client tab completion of command names
        if (command instanceof ICommandGroup group)
        {
            for (ISubCommand subCommand : group.getSubCommands())
            {
                node.then(bridge(Commands.literal(subCommand.getName().toLowerCase()), subCommand));
            }
        }

        // Raw arguments
        node.then(Commands.argument(ARGS, StringArgumentType.greedyString())
            .suggests((context, builder) -> suggest(context, builder, command))
            .executes(context -> run(context, command, splitArgs(StringArgumentType.getString(context, ARGS)))));
        return node;
    }

    private static int run(CommandContext<CommandSourceStack> context, ISubCommand command, String[] args) throws CommandSyntaxException
    {
        final CommandSourceStack source = context.getSource();
        command.handleCommand(source.getServer(), source, args);
        return Command.SINGLE_SUCCESS;
    }

    /**
     * Splits raw input the same way the 1.12 command handler did
     *
     * @param input - raw arguments
     * @return arguments
     */
    public static String[] splitArgs(String input)
    {
        if (input == null || input.isEmpty())
        {
            return new String[0];
        }
        return input.split(" ");
    }

    private static CompletableFuture<Suggestions> suggest(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder, ISubCommand command)
    {
        final String remaining = builder.getRemaining();

        // Keep trailing empty entry so the last word being typed is always the last arg, matches 1.12 tab behaviour
        final String[] args = remaining.split(" ", -1);
        final List<String> suggestions = command.getTabSuggestions(context.getSource().getServer(), context.getSource(), args, null);

        final SuggestionsBuilder lastWord = builder.createOffset(builder.getStart() + remaining.lastIndexOf(' ') + 1);
        if (suggestions != null)
        {
            suggestions.forEach(lastWord::suggest);
        }
        return lastWord.buildFuture();
    }
}
// PORTED
