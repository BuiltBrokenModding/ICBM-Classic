package icbm.classic.command.sub;

import icbm.classic.command.CommandUtils;
import icbm.classic.command.system.SubCommand;
import icbm.classic.lib.explosive.ExplosiveHandler;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentTranslation;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Created by Dark(DarkGuardsman, Robert) on 4/13/2018.
 */
public class CommandLag extends SubCommand
{
    public static final String TRANSLATION_LAG_REMOVE = "command.icbmclassic:icbm.lag.remove";
    private final Predicate<Entity> icbmEntitySelector = (entity) -> entity.isEntityAlive() && CommandUtils.isICBMEntity(entity);

    public CommandLag()
    {
        super("lag");
    }

    @Override
    protected void collectHelpForAll(Consumer<String> consumer)
    {

    }

    @Override
    protected void collectHelpWorldOnly(Consumer<String> consumer)
    {
        consumer.accept("[radius]");
    }

    @Override
    public void handleCommand(@Nonnull MinecraftServer server, @Nonnull ICommandSender sender, @Nonnull String[] args)
    {
        //Parse range
        double range = args.length > 1 ? Double.parseDouble(args[1]) : 1000;

        //Remove ICBM entities
        final List<Entity> entities = CommandUtils.getEntities(sender.getEntityWorld(),
                sender.getPositionVector().x, sender.getPositionVector().y, sender.getPositionVector().z,
                range,
                icbmEntitySelector);
        entities.forEach(Entity::setDead);

        //Remove blasts queue to run or currently running
        final int blastRemoveCount = ExplosiveHandler.removeNear(sender.getEntityWorld(),
                sender.getPositionVector().x, sender.getPositionVector().y, sender.getPositionVector().z,
                range);

        //Update user with data
        sender.sendMessage(new TextComponentTranslation(TRANSLATION_LAG_REMOVE, blastRemoveCount, entities.size(), range));
    }
}
