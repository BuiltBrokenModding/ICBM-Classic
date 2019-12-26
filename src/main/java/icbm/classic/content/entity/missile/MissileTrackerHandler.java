package icbm.classic.content.entity.missile;

import icbm.classic.ICBMConstants;
import net.minecraft.world.World;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.HashMap;

/**
 * Handles tracking and simulating the missiles outside of the game world
 *
 *
 * Created by Dark(DarkGuardsman, Robert) on 8/4/2018.
 */
@Mod.EventBusSubscriber(modid = ICBMConstants.DOMAIN)
public class MissileTrackerHandler
{
    /** World save data key */
    private static final String DATA_SAVE_ID = ICBMConstants.DOMAIN + "MissileTracker";

    /** Map of handlers per dimension <DimensionID, Handler> */
    private static final HashMap<Integer, MissileTrackerWorld> dimToHandlerMap = new HashMap<>();

    /**
     * Called to simulate the missile
     * <p>
     * This will destroy the missile and load it's data into the simulate.
     * Which will then tick down until the missile should be spawned again.
     *
     * @param missile - entity to simulate
     */
    public static void simulateMissile(EntityMissile missile)
    {
        getOrCreateHandler(missile.world, true).simulateMissile(missile);
    }

    /**
     * Gets the handler for the world
     * <p>
     * Will load the handler if save data exists in the world. If not
     * it will create a new handler for use. May return NULL if
     * create is false and the handler was not found!
     *
     * @param world  - world instance
     * @param create - build handler if missing
     * @return handler
     */
    public static MissileTrackerWorld getOrCreateHandler(World world, boolean create)
    {
        String trackerName = DATA_SAVE_ID + world.provider.getDimension();
        //Get handler from map
        if (dimToHandlerMap.containsKey(world.provider.getDimension()))
        {
            return dimToHandlerMap.get(world.provider.getDimension());
        }

        if (create)
        {
            //Try to get handler from world save
            MissileTrackerWorld instance = (MissileTrackerWorld) world.getPerWorldStorage().getOrLoadData(MissileTrackerWorld.class, trackerName);

            //If missing create
            if (instance == null)
            {
                instance = new MissileTrackerWorld(trackerName);
                world.getPerWorldStorage().setData(trackerName, instance);
            }

            dimToHandlerMap.put(world.provider.getDimension(), instance);
            return instance;
        }
        else
        {
            return dimToHandlerMap.getOrDefault(world.provider.getDimension(), null);
        }
    }

    @SubscribeEvent
    public static void onWorldLoad(WorldEvent.Load event)
    {
        if (!event.getWorld().isRemote)
        {
            getOrCreateHandler(event.getWorld(),true); // load handlers, to make missiles continue their flight
        }
    }

    @SubscribeEvent
    public static void onWorldUnload(WorldEvent.Unload event) // used to destroy existing handlers on unload
    {
        if (!event.getWorld().isRemote)
        {
            MissileTrackerWorld handler = getOrCreateHandler(event.getWorld(), false);
            if (handler != null)
            {
                handler.destroy();
                dimToHandlerMap.remove(event.getWorld().provider.getDimension());
            }
        }
    }

    @SubscribeEvent
    public static void onWorldTick(TickEvent.WorldTickEvent event)
    {
        if (!event.world.isRemote)
        {
            MissileTrackerWorld handler = getOrCreateHandler(event.world, false);
            if (handler != null)
            {
                handler.onWorldTick(event.world);
            }
        }
    }

}
