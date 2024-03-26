package icbm.classic.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import icbm.classic.lib.explosive.ExplosiveHandler;
import icbm.classic.world.IcbmEntityTypes;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.ResourceKeyArgument;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.server.command.EnumArgument;

import java.util.List;

import static com.mojang.brigadier.arguments.DoubleArgumentType.doubleArg;
import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class IcbmCommand {

    @SuppressWarnings("unchecked")
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(literal("icbm")
            .requires(cs -> cs.hasPermission(2))
            .then(literal("remove")
                .then(argument("type", EnumArgument.enumArgument(RemoveType.class))
                    .executes(context -> remove(context,
                        context.getArgument("type", RemoveType.class),
                        context.getSource().getLevel().dimension(),
                        context.getSource().getPosition().x,
                        context.getSource().getPosition().y,
                        context.getSource().getPosition().z,
                        -1))
                    .then(argument("dim", ResourceKeyArgument.key(Registries.DIMENSION))
                        .then(argument("x", doubleArg())
                            .then(argument("y", doubleArg())
                                .then(argument("z", doubleArg())
                                    .then(argument("range", doubleArg())
                                        .executes(context -> remove(context,
                                            context.getArgument("type", RemoveType.class),
                                            context.getArgument("dim", ResourceKey.class),
                                            context.getArgument("x", Double.class),
                                            context.getArgument("y", Double.class),
                                            context.getArgument("z", Double.class),
                                            context.getArgument("range", Double.class))))))))
                    .then(argument("range", doubleArg())
                        .executes(context -> remove(context,
                            context.getArgument("type", RemoveType.class),
                            context.getSource().getLevel().dimension(),
                            context.getSource().getPosition().x,
                            context.getSource().getPosition().y,
                            context.getSource().getPosition().z,
                            context.getArgument("range", Double.class))))))
            .then(literal("lag")
                .executes(context -> lag(context, 1000))
                .then(argument("range", doubleArg(0))
                    .executes(context -> lag(context, context.getArgument("range", Double.class))))));
    }

    private static int remove(CommandContext<CommandSourceStack> context, RemoveType type, ResourceKey<Level> dimension,
                              double x, double y, double z, double range) {

        Iterable<Entity> allEntities = context.getSource().getLevel().getAllEntities();
        int removed = 0;
        for (Entity entity : allEntities) {
            if (range < 0 || entity.distanceToSqr(x, y, z) <= range * range) {
                if (entity.getType().is(type.removeHolder)) {
                    entity.remove(Entity.RemovalReason.KILLED);
                    removed++;
                }
            }
        }

        context.getSource().sendSystemMessage(Component.translatable("command.icbmclassic:icbm.remove",
            removed, range));

        return 0;
    }

    private static int lag(CommandContext<CommandSourceStack> context, double range) {
        CommandSourceStack source = context.getSource();
        List<Entity> entities = context.getSource().getLevel().getEntities(source.getEntity(), AABB.ofSize(source.getPosition(),
            range * 2, range * 2, range * 2));
        int removed = 0;
        for (Entity entity : entities) {
            if (entity.getType().is(IcbmEntityTypes.all())) {
                entity.remove(Entity.RemovalReason.KILLED);
                removed++;
            }
        }

        int blastRemoveCount = ExplosiveHandler.removeNear(source.getLevel(), source.getPosition(), range);

        source.sendSystemMessage(Component.translatable("command.icbmclassic:icbm.lag.remove",
            blastRemoveCount, removed, range));

        return 0;
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    private enum RemoveType {
        all(IcbmEntityTypes.all()),
        missiles(IcbmEntityTypes.missiles()),
        explosions(IcbmEntityTypes.explosions());

        private final HolderSet<EntityType<?>> removeHolder;
    }
}
