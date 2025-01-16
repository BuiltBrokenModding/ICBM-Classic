package icbm.classic.lib.actions;

import icbm.classic.ICBMConstants;
import icbm.classic.api.explosion.IBlast;
import icbm.classic.content.blast.Blast;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

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
        if (!event.getWorld().isRemote() && !activeBlasts.isEmpty())
        {
            final DimensionType dim = event.getWorld().getDimension().getType();
            activeBlasts.stream()
                    .filter(blast -> blast.getWorld() == null || blast.getWorld().getDimension().getType() == dim)
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
        final Vec3d pos = new Vec3d(x, y, z);

        //Collect blasts marked for removal
        final List<IBlast> toRemove = WorkTickingActionHandler.activeBlasts.stream()
                .filter(blast -> blast.getWorld() == world)
                .filter(blast -> range < 0 || range > 0 && range > Math.sqrt(pos.squareDistanceTo(blast.getPosition())))
                .collect(Collectors.toList());

        //Do removals
        activeBlasts.removeAll(toRemove);
        toRemove.forEach(IBlast::clearBlast);

        return toRemove.size();
    }
}
