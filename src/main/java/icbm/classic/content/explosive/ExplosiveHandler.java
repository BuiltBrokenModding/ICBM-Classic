package icbm.classic.content.explosive;

import icbm.classic.ICBMClassic;
import icbm.classic.content.explosive.blast.Blast;
import net.minecraft.world.World;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Simple handler to track blasts in order to disable or remove
 *
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 4/9/2018.
 */
@Mod.EventBusSubscriber(modid = ICBMClassic.DOMAIN)
public class ExplosiveHandler
{
    public static final ArrayList<Blast> activeBlasts = new ArrayList();

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
        if (!event.getWorld().isRemote)
        {
            Iterator<Blast> it = activeBlasts.iterator();
            while (it.hasNext())
            {
                Blast next = it.next();
                if (next.world == null || next.world.provider.getDimension() == event.getWorld().provider.getDimension())
                {
                    onKill(next);
                    it.remove();
                }
            }
        }
    }

    /**
     * Runs kill logic on the blast, does not remove the blast
     *
     * @param blast
     */
    public static void onKill(Blast blast)
    {
        if (blast.getThread() != null)
        {
            blast.getThread().kill();
        }
        blast.isAlive = false; //TODO replace with method to allow blast to cleanup
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
        int removeCount = 0;
        Iterator<Blast> it = ExplosiveHandler.activeBlasts.iterator();
        while (it.hasNext())
        {
            Blast blast = it.next();
            if (blast.world == world && (range < 0 || range > 0 && range > blast.location.distance(x, y, z)))
            {
                ExplosiveHandler.onKill(blast);
                it.remove();
                removeCount++;
            }
        }
        return removeCount;
    }
}
