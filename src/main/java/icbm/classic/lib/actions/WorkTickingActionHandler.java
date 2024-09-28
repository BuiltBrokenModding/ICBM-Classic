package icbm.classic.lib.actions;

import icbm.classic.ICBMConstants;
import icbm.classic.api.explosion.IBlast;
import icbm.classic.content.blast.Blast;
import icbm.classic.lib.transform.vector.Pos;
import net.minecraft.world.World;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Simple handler to track blasts in order to disable or remove
 * <p>
 * <p>
 * Created by Dark(DarkGuardsman, Robin) on 4/9/2018.
 */
@Mod.EventBusSubscriber(modid = ICBMConstants.DOMAIN)
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
    public static void worldUnload(WorldEvent.Unload event)
    {
        if (!event.getWorld().isRemote && !activeBlasts.isEmpty())
        {
            final int dim = event.getWorld().provider.getDimension();
            activeBlasts.stream()
                    .filter(blast -> blast != null && (blast.world() == null || blast.world().provider.getDimension() == dim))
                    .forEach(IBlast::clearBlast);
        }
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
    public static int removeNear(World world, double x, double y, double z, double range)
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
