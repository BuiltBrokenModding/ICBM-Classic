package icbm.classic.content.missile.tracker;

import icbm.classic.ICBMConstants;
import icbm.classic.api.events.MissileEvent;
import icbm.classic.content.missile.entity.explosive.EntityExplosiveMissile;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;

/**
 * Handles tracking and simulating the missiles outside of the game world
 *
 *
 * Created by Dark(DarkGuardsman, Robin) on 8/4/2018.
 */
@Mod.EventBusSubscriber(modid = ICBMConstants.DOMAIN)
public class MissileTrackerHandler
{
    /** World save data key */
    private static final String DATA_SAVE_ID = ICBMConstants.DOMAIN + "MissileTracker_";

    /** Map of handlers per dimension <DimensionID, Handler> */
    private static final HashMap<Integer, MissileTrackerWorld> dimToHandlerMap = new HashMap<>();

    /**
     * Called to simulate the missile
     * <p>
     * This will destroy the missile and load it's data into the simulate.
     * Which will then tick down until the missile should be spawned again.
     *
     * @param missile - entity to simulate
     * @return true if missile was added to simulate queue, false if blocked
     */
    public static boolean simulateMissile(EntityExplosiveMissile missile)
    {
        //TODO add an event to capture missile and change how simulation works... specifically AR support
        // Can't save missiles that are dead, riding another entity, or have a player riding
        if(missile != null && missile.world instanceof ServerWorld && missile.isAlive() && !missile.hasPlayerRiding()) {

            // Fire event to allow canceling simulation
            final MissileEvent.EnteringSimQueue event = new MissileEvent.EnteringSimQueue(missile.getMissileCapability(), missile);
            if(MinecraftForge.EVENT_BUS.post(event)) {
                return false;
            }

            getOrCreateHandler((ServerWorld) missile.world, true).simulateMissile(missile);
        }
        return false;
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
    public static MissileTrackerWorld getOrCreateHandler(ServerWorld world, boolean create)
    {
        final String trackerName = DATA_SAVE_ID + DimensionType.getKey(world.getDimension().getType());
        final int key = world.getDimension().getType().getId();
        //Get handler from map
        if (dimToHandlerMap.containsKey(key))
        {
            return dimToHandlerMap.get(key);
        }

        if (create)
        {
            //Try to get handler from world save
            MissileTrackerWorld instance = world.getSavedData().getOrCreate(() -> new MissileTrackerWorld(trackerName), trackerName);
            dimToHandlerMap.put(key, instance);
            return instance;
        }
        else
        {
            return dimToHandlerMap.getOrDefault(key, null);
        }
    }

    @SubscribeEvent
    public static void onWorldLoad(WorldEvent.Load event)
    {
        if (event.getWorld() instanceof ServerWorld)
        {
            getOrCreateHandler((ServerWorld) event.getWorld(),true); // load handlers, to make missiles continue their flight
        }
    }

    @SubscribeEvent
    public static void onWorldUnload(WorldEvent.Unload event) // used to destroy existing handlers on unload
    {
        if (event.getWorld() instanceof ServerWorld)
        {
            MissileTrackerWorld handler = getOrCreateHandler((ServerWorld) event.getWorld(), false);
            if (handler != null)
            {
                handler.destroy();
                dimToHandlerMap.remove(event.getWorld().getDimension().getType().getId());
            }
        }
    }

    @SubscribeEvent
    public static void onWorldTick(TickEvent.WorldTickEvent event)
    {
        if (event.world instanceof ServerWorld)
        {
            MissileTrackerWorld handler = getOrCreateHandler((ServerWorld)event.world, false);
            if (handler != null)
            {
                handler.onWorldTick((ServerWorld) event.world);
            }
        }
    }

}
