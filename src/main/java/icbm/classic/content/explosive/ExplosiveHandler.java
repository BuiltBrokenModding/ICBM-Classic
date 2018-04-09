package icbm.classic.content.explosive;

import icbm.classic.ICBMClassic;
import icbm.classic.content.explosive.blast.Blast;
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
                    if (next.getThread() != null)
                    {
                        next.getThread().kill();
                    }
                    next.isAlive = false;
                    it.remove();
                }
            }
        }
    }
}
