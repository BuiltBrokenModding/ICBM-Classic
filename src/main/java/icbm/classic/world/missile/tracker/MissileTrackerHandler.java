package icbm.classic.world.missile.tracker;

import icbm.classic.IcbmConstants;
import icbm.classic.world.missile.entity.EntityMissile;
import icbm.classic.world.missile.entity.explosive.ExplosiveMissileEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.event.world.WorldEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.common.eventhandler.SubscribeEvent;
import net.neoforged.fml.common.gameevent.TickEvent;

import java.util.HashMap;

/**
 * Handles tracking and simulating the missiles outside of the game world
 * <p>
 * <p>
 * Created by Dark(DarkGuardsman, Robert) on 8/4/2018.
 */
@Mod.EventBusSubscriber(modid = IcbmConstants.MOD_ID)
public class MissileTrackerHandler {
    /**
     * Level save data key
     */
    private static final String DATA_SAVE_ID = IcbmConstants.MOD_ID + "MissileTracker";

    /**
     * Map of handlers per dimension <DimensionID, Handler>
     */
    private static final HashMap<Integer, MissileTrackerWorld> dimToHandlerMap = new HashMap<>();

    /**
     * Called to simulate the missile
     * <p>
     * This will destroy the missile and load it's data into the simulate.
     * Which will then tick down until the missile should be spawned again.
     *
     * @param missile - entity to simulate
     */
    public static void simulateMissile(ExplosiveMissileEntity missile) {
        // Can't save missiles that are dead, riding another entity, or have a player riding
        if (missile != null && !missile.isDead && !missile.isRiding() && noPlayer(missile)) {
            getOrCreateHandler(missile.world, true).simulateMissile(missile);
        }
    }

    private static boolean noPlayer(EntityMissile missile) {
        // TODO check for riding chains
        return missile.getPassengers().stream().noneMatch(e -> e instanceof Player);
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
    public static MissileTrackerLevel getOrCreateHandler(Level level, boolean create) {
        String trackerName = DATA_SAVE_ID + world.provider.getDimension();
        //Get handler from map
        if (dimToHandlerMap.containsKey(world.provider.getDimension())) {
            return dimToHandlerMap.get(world.provider.getDimension());
        }

        if (create) {
            //Try to get handler from world save
            MissileTrackerLevel instance = (MissileTrackerWorld) world.getPerWorldStorage().getOrLoadData(MissileTrackerWorld.class, trackerName);

            //If missing create
            if (instance == null) {
                instance = new MissileTrackerLevel(trackerName);
                world.getPerWorldStorage().setData(trackerName, instance);
            }

            dimToHandlerMap.put(world.provider.getDimension(), instance);
            return instance;
        } else {
            return dimToHandlerMap.getOrDefault(world.provider.getDimension(), null);
        }
    }

    @SubscribeEvent
    public static void onWorldLoad(WorldEvent.Load event) {
        if (!event.getLevel().isClientSide()) {
            getOrCreateHandler(event.getLevel(), true); // load handlers, to make missiles continue their flight
        }
    }

    @SubscribeEvent
    public static void onWorldUnload(WorldEvent.Unload event) // used to destroy existing handlers on unload
    {
        if (!event.getLevel().isClientSide()) {
            MissileTrackerLevel handler = getOrCreateHandler(event.getLevel(), false);
            if (handler != null) {
                handler.destroy();
                dimToHandlerMap.remove(event.getLevel().provider.getDimension());
            }
        }
    }

    @SubscribeEvent
    public static void onWorldTick(TickEvent.WorldTickEvent event) {
        if (!event.world.isClientSide()) {
            MissileTrackerLevel handler = getOrCreateHandler(event.world, false);
            if (handler != null) {
                handler.onWorldTick(event.world);
            }
        }
    }

}
