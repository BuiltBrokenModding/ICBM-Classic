package icbm.classic.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import icbm.classic.ICBMClassic;
import icbm.classic.api.explosion.BlastState;
import icbm.classic.api.explosion.responses.BlastResponse;
import icbm.classic.api.reg.ExplosiveType;
import icbm.classic.core.registries.IcbmRegistries;
import icbm.classic.lib.explosive.ExplosiveHandler;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

import static com.mojang.brigadier.arguments.DoubleArgumentType.doubleArg;
import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;
import static net.minecraft.commands.arguments.ResourceKeyArgument.key;

public class BlastCommand {

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

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(literal("blast")
            .then(literal("spread")
                .then(argument("count", integer())
                    .then(argument("distance", integer())
                        .then(argument("id", key(IcbmRegistries.EXPLOSIVES))
                            .then(argument("dim", key(Registries.DIMENSION))
                                .then(argument("x", doubleArg())
                                    .then(argument("y", doubleArg())
                                        .then(argument("z", doubleArg())
                                            .then(argument("scale", doubleArg())
                                                .executes(BlastCommand::spread))))))))))
            .then(literal("trigger")
                .then(argument("id", key(IcbmRegistries.EXPLOSIVES))
                    .then(argument("scale", doubleArg())
                        .executes(BlastCommand::triggerShort))
                    .then(argument("dim", key(Registries.DIMENSION))
                        .then(argument("x", doubleArg())
                            .then(argument("y", doubleArg())
                                .then(argument("z", doubleArg())
                                    .then(argument("scale", doubleArg())
                                        .executes(BlastCommand::triggerLong)))))))));
    }

    @SuppressWarnings("unchecked")
    private static int triggerShort(CommandContext<CommandSourceStack> context) {
        return trigger(context,
            context.getSource().getLevel(),
            context.getSource().getPosition().x,
            context.getSource().getPosition().y,
            context.getSource().getPosition().z,
            context.getArgument("id", ResourceKey.class),
            context.getArgument("scale", Double.class));
    }

    @SuppressWarnings("unchecked")
    private static int triggerLong(CommandContext<CommandSourceStack> context) {
        return trigger(context,
            context.getSource().getServer().getLevel(context.getArgument("id", ResourceKey.class)),
            context.getArgument("x", Double.class),
            context.getArgument("y", Double.class),
            context.getArgument("z", Double.class),
            context.getArgument("id", ResourceKey.class),
            context.getArgument("scale", Double.class));
    }

    private static int trigger(CommandContext<CommandSourceStack> context, Level level, double x, double y, double z,
                               ResourceKey<ExplosiveType> data, double scale) {
        BlastResponse result = ExplosiveHandler.createExplosion(null, level, x, y, z, data, scale, null);

        context.getSource().sendSystemMessage(Component.translatable(getTranslationKey(result.state),
            data, scale, level.dimension().location(), level.dimensionTypeId().location(), x, y, z));
        return 0;
    }

    @SuppressWarnings("unchecked")
    private static int spread(CommandContext<CommandSourceStack> context) {
        int count = context.getArgument("count", Integer.class);
        int distance = context.getArgument("distance", Integer.class);
        ResourceKey<ExplosiveType> id = context.getArgument("id", ResourceKey.class);
        ResourceKey<Level> dimension = context.getArgument("dim", ResourceKey.class);
        double x = context.getArgument("x", Double.class);
        double y = context.getArgument("y", Double.class);
        double z = context.getArgument("z", Double.class);
        double scale = context.getArgument("scale", Double.class);

        if (scale <= 0) {
            context.getSource().sendSystemMessage(Component.translatable(TRANSLATION_ERROR_SCALE_ZERO));
            return 1;
        }

        ServerLevel level = context.getSource().getServer().getLevel(dimension);

        int expectedSpawnCount = (int) Math.floor(Math.pow((count * 2) + 1, 2));

        context.getSource().sendSystemMessage(Component.translatable("command.icbmclassic:icbm.spread.started",
            id.location(), scale, dimension.location(), x, y, z, count, distance, expectedSpawnCount));

        for (int xi = -count; xi <= count; xi++) {
            for (int zi = -count; zi <= count; zi++) {
                // Calculate position
                double xPos = x + xi * distance;
                double zPos = z + zi * distance;

                // Trigger blast
                BlastResponse result = ExplosiveHandler.createExplosion(null, level, xPos, y, zPos, id, scale, null);

                if (result.state != BlastState.TRIGGERED && result.state != BlastState.THREADING) {
                    // Send translated message to user
                    context.getSource().sendSystemMessage(Component.translatable(getTranslationKey(result.state), //TODO handle sub-error
                        id.location(), scale, dimension.location(), xPos, y, zPos));
                }
            }
        }
        return 0;
    }

    private static String getTranslationKey(BlastState result) {
        return switch (result) {
            case TRIGGERED -> TRANSLATION_TRIGGERED;
            case THREADING -> TRANSLATION_THREADING;
            case CANCLED -> TRANSLATION_ERROR_BLOCKED;
            case ERROR -> TRANSLATION_ERROR;
            case ALREADY_TRIGGERED -> TRANSLATION_ERROR_TRIGGERED;
            default -> {
                ICBMClassic.logger().error("CommandBlastTrigger: unknown blast status code " + result);
                yield TRANSLATION_ERROR_UNKNOWN;
            }
        };
    }
}
