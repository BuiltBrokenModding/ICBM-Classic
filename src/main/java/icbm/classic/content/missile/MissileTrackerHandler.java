package icbm.classic.content.missile;

import icbm.classic.ICBMClassic;
import net.minecraft.world.World;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.HashMap;

/**
 * Handles tracking and simulating the missiles outside of the game world
 *
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 8/4/2018.
 */
public class MissileTrackerHandler
{
    /** World save data key */
    public static final String DATA_SAVE_ID = ICBMClassic.DOMAIN + "MissileTracker";

    /** Map of handlers per dimension <DimensionID, Handler> */
    public static final HashMap<Integer, MissileTrackerWorld> dimToHandlerMap = new HashMap();

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
        getHandler(missile.world, true).simulateMissile(missile);
    }

    /**
     * Gets the handler for the world
     * <p>
     * Will load the handler if save data exists in the world. If not
     * it will create a new handler for use.
     *
     * @param world  - world instance
     * @param create - build handler if missing
     * @return handler
     */
    public static MissileTrackerWorld getHandler(World world, boolean create)
    {
        //Get handler from map
        if (dimToHandlerMap.containsKey(world.provider.getDimension()))
        {
            return dimToHandlerMap.get(world.provider.getDimension());
        }

        //Try to get handler from world save
        MissileTrackerWorld instance = (MissileTrackerWorld) world.getPerWorldStorage().getOrLoadData(MissileTrackerWorld.class, DATA_SAVE_ID);

        if (create)
        {
            //If missing create
            if (instance == null)
            {
                instance = new MissileTrackerWorld(DATA_SAVE_ID);
                world.getPerWorldStorage().setData(DATA_SAVE_ID, instance);
            }

            dimToHandlerMap.put(world.provider.getDimension(), instance);
        }

        return instance;
    }

    @SubscribeEvent
    public static void onWorldUnload(WorldEvent.Unload event) // used to destroy existing handlers on unload
    {
        if (!event.getWorld().isRemote)
        {
            MissileTrackerWorld handler = getHandler(event.getWorld(), false);
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
            MissileTrackerWorld handler = getHandler(event.world, false);
            if (handler != null)
            {
                handler.onWorldTick(event.world);
            }
        }
    }

}
