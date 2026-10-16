package icbm.classic.lib.actions;

import icbm.classic.ICBMClassic;
import icbm.classic.ICBMConstants;
import icbm.classic.api.explosion.IBlast;
import icbm.classic.content.blast.Blast;
import icbm.classic.lib.transform.vector.Pos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Simple handler to track blasts in order to disable or remove
 * <p>
 * <p>
 * Created by Dark(DarkGuardsman, Robin) on 4/9/2018.
 */
@EventBusSubscriber(modid = ICBMConstants.DOMAIN)
public class WorkTickingActionHandler //TODO create interface that is related to actions but isn't an action to replace this (IWorldTicking)
{
    public static final ArrayList<IBlast> activeBlasts = new ArrayList();

    public static void add(Blast blast)
    {
        activeBlasts.add(blast);
    }

    public static void remove(Blast blast)
    {
        activeBlasts.remove(blast);
    }

    @SubscribeEvent
    public static void worldUnload(LevelEvent.Unload event)
    {
        if (event.getLevel() instanceof Level level && !level.isClientSide() && !activeBlasts.isEmpty())
        {
            final ResourceKey<Level> dim = level.dimension();

            // PORT: 1.12 streamed activeBlasts directly while clearBlast -> endBlast removed from it, which throws
            // ConcurrentModificationException. In 1.21 that escapes MinecraftServer#stopServer and leaves the
            // remaining levels and the session lock open. Collect first, drop from the list, then clear.
            final List<IBlast> toClear = activeBlasts.stream()
                    .filter(blast -> blast == null || blast.world() == null || blast.world().dimension() == dim)
                    .collect(Collectors.toList());
            activeBlasts.removeAll(toClear);

            for (IBlast blast : toClear)
            {
                if (blast == null)
                {
                    continue;
                }
                try
                {
                    // PORT: 1.12 posted WorldEvent.Unload after the world was saved, so killing the controller entity
                    // never reached the save. In 1.21 ServerLevel#close saves entities again after LevelEvent.Unload,
                    // discarding a live controller here would delete the running blast (gas, sonic, beam...) from the
                    // save. Leave the entity alone, it restores the blast on load.
                    final Entity controller = blast.getEntity();
                    if (controller != null && !controller.isRemoved())
                    {
                        if (blast instanceof Blast b && b.getThread() != null)
                        {
                            b.getThread().kill();
                        }
                        continue;
                    }
                    blast.clearBlast();
                }
                catch (Exception e)
                {
                    ICBMClassic.logger().error("Failed to clear blast on world unload " + blast, e);
                }
            }
        }
    }

    /**
     * Blasts hold their level, drop anything left over once the server is gone so old levels
     * are not kept alive between single player sessions.
     */
    @SubscribeEvent
    public static void serverStopped(ServerStoppedEvent event)
    {
        activeBlasts.clear();
    }

    /**
     * Called to remove blasts near the location
     *
     * @param world = position
     * @param x     - position
     * @param y     - position
     * @param z     - position
     * @param range - distance from position, less than zero will turn into global
     * @return number of blasts removed
     */
    public static int removeNear(Level world, double x, double y, double z, double range)
    {
        final Pos pos = new Pos(x, y, z);

        //Collect blasts marked for removal
        final List<IBlast> toRemove = WorkTickingActionHandler.activeBlasts.stream()
                .filter(blast -> blast.world() == world)
                .filter(blast -> range < 0 || range > 0 && range > pos.distance(blast))
                .collect(Collectors.toList());

        //Do removals
        activeBlasts.removeAll(toRemove);
        toRemove.forEach(IBlast::clearBlast);

        return toRemove.size();
    }
}
// PORTED
