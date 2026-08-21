package icbm.classic.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.content.blast.redmatter.EntityRedmatter;
import icbm.classic.content.cluster.bomblet.EntityBombDroplet;
import icbm.classic.content.entity.EntityExplosion;
import icbm.classic.content.entity.EntityExplosive;
import icbm.classic.content.entity.EntityExplosiveFragment;
import icbm.classic.content.entity.EntityGrenade;
import icbm.classic.content.entity.flyingblock.EntityFlyingBlock;
import icbm.classic.lib.actions.WorkTickingActionHandler;
import lombok.var;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TranslationTextComponent;

public class ICBMCommands {
    private static final int PERMISSION_LEVEL_OP = 3;

    public static void setupCommands(CommandDispatcher<CommandSource> dispatcher) {
        LiteralArgumentBuilder<CommandSource> icbm = Commands.literal("icbm").requires(x -> x.hasPermissionLevel(PERMISSION_LEVEL_OP));
        //icbm.then(Commands.literal("blast"))
        //icbm.then(Commands.literal("remove"))
        icbm.then(
            Commands.literal("lag")
                .executes(ctx -> commandLag(ctx, 1000))

                // variant with optional radius argument
                .then(Commands.argument("radius", IntegerArgumentType.integer(1))
                    .executes(ctx -> commandLag(ctx, IntegerArgumentType.getInteger(ctx, "radius")))
                )
        );


        dispatcher.register(icbm);
    }


    private static int commandLag(CommandContext<CommandSource> ctx, int range) {
        var sender = ctx.getSource();
        var world = sender.getWorld();
        var rangeVec = new Vec3d(range, range, range);

        //Remove blasts queue to run or currently running
        final int blastRemoveCount = WorkTickingActionHandler.removeNear(world, sender.getPos(), range);

        //Remove ICBM entities
        var entities = world.getEntitiesWithinAABB(Entity.class,
            new AxisAlignedBB(sender.getPos().subtract(rangeVec), sender.getPos().add(rangeVec)),
            (entity) -> entity.isAlive() && isICBMEntity(entity));

        for (var ent : entities) {
            ent.onKillCommand();
        }

        //Update user with data
        sender.sendFeedback(new TranslationTextComponent("command.icbm.icbm.lag.remove", blastRemoveCount, entities.size(), range), true);
        return Command.SINGLE_SUCCESS;
    }

    public static boolean isICBMEntity(Entity entity) {
        return entity instanceof EntityExplosiveFragment
               || entity instanceof EntityFlyingBlock
               || entity instanceof EntityBombDroplet
               || entity instanceof EntityExplosive
               || entity instanceof EntityExplosion
               || entity instanceof EntityGrenade
               || entity instanceof EntityRedmatter
               || isMissile(entity);
    }

    public static boolean isMissile(Entity entity) {
        return entity.getCapability(ICBMClassicAPI.MISSILE_CAPABILITY, null).isPresent();
    }
}
