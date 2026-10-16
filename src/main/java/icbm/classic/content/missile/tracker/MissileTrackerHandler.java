package icbm.classic.content.missile.tracker;

import icbm.classic.ICBMConstants;
import icbm.classic.api.events.MissileEvent;
import icbm.classic.content.missile.entity.EntityMissile;
import icbm.classic.content.missile.entity.explosive.EntityExplosiveMissile;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.world.chunk.RegisterTicketControllersEvent;
import net.neoforged.neoforge.common.world.chunk.TicketController;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

import java.util.HashMap;

/**
 * Handles tracking and simulating the missiles outside of the game world
 *
 *
 * Created by Dark(DarkGuardsman, Robin) on 8/4/2018.
 */
@EventBusSubscriber(modid = ICBMConstants.DOMAIN)
public class MissileTrackerHandler
{
    /** Level save data key */
    private static final String DATA_SAVE_ID = ICBMConstants.DOMAIN + "MissileTracker";

    /**
     * PORT: replaces the 1.12 ForgeChunkManager ticket. Chunks still forced from a previous run are released
     * on load, 1.12 queued them for release after the normal unload cooldown.
     */
    public static final TicketController CHUNK_TICKETS = new TicketController(
        ResourceLocation.fromNamespaceAndPath(ICBMConstants.DOMAIN, "missile_tracker"),
        (level, ticketHelper) -> {
            ticketHelper.getEntityTickets().keySet().forEach(ticketHelper::removeAllTickets);
            ticketHelper.getBlockTickets().keySet().forEach(ticketHelper::removeAllTickets);
        });

    /** Map of handlers per dimension <DimensionID, Handler> */
    private static final HashMap<ResourceKey<Level>, MissileTrackerWorld> dimToHandlerMap = new HashMap<>();

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
        if(missile != null && missile.isAlive() && !missile.isPassenger() && noPlayer(missile)) {

            // Fire event to allow canceling simulation
            final MissileEvent.EnteringSimQueue event = new MissileEvent.EnteringSimQueue(missile.getMissileCapability(), missile);
            if(NeoForge.EVENT_BUS.post(event).isCanceled()) {
                return false;
            }

            final MissileTrackerWorld handler = getOrCreateHandler(missile.level(), true);
            if (handler != null) {
                handler.simulateMissile(missile);
            }
        }
        return false;
    }

    /**
     * PORT: 1.12 entities ticked in every loaded chunk. In 1.21 a chunk can be loaded (inside view distance)
     * while entities in it are frozen (outside simulation distance or a border chunk). Missiles use this
     * instead of "is chunk loaded" so they are simulated before flying into a chunk where they would freeze.
     *
     * @param level  - server level
     * @param chunkX - chunk x
     * @param chunkZ - chunk z
     * @return true if entities in the chunk will tick
     */
    public static boolean canEntitiesTick(ServerLevel level, int chunkX, int chunkZ)
    {
        final ChunkPos chunkPos = new ChunkPos(chunkX, chunkZ);
        // Same two checks ServerLevel#tick applies to entities: the entity section is ticking (ENTITY_TICKING chunk)
        // and the chunk is in entity ticking range of the simulation distance or a ticking forced chunk
        return level.isNaturalSpawningAllowed(chunkPos) && level.isNaturalSpawningAllowed(chunkPos.getWorldPosition());
    }

    /**
     * Checks every chunk in the flat box between two positions, see {@link #canEntitiesTick(ServerLevel, int, int)}
     *
     * @return true if entities tick in all of the chunks
     */
    public static boolean canEntitiesTickBetween(ServerLevel level, double fromX, double fromZ, double toX, double toZ)
    {
        // PORT: 1.12 looped start..end and skipped the check entirely for missiles moving towards -x or -z
        final int minX = Mth.floor(Math.min(fromX, toX)) >> 4;
        final int maxX = Mth.floor(Math.max(fromX, toX)) >> 4;
        final int minZ = Mth.floor(Math.min(fromZ, toZ)) >> 4;
        final int maxZ = Mth.floor(Math.max(fromZ, toZ)) >> 4;
        for (int chunkX = minX; chunkX <= maxX; ++chunkX)
        {
            for (int chunkZ = minZ; chunkZ <= maxZ; ++chunkZ)
            {
                if (!canEntitiesTick(level, chunkX, chunkZ))
                {
                    return false;
                }
            }
        }
        return true;
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
    public static MissileTrackerWorld getOrCreateHandler(Level world, boolean create)
    {
        //Get handler from map
        if (dimToHandlerMap.containsKey(world.dimension()))
        {
            return dimToHandlerMap.get(world.dimension());
        }

        if (create && world instanceof ServerLevel serverLevel)
        {
            // PORT: storage is already per dimension, 1.12 appended the int dim id which is not a valid file name as a key
            final MissileTrackerWorld instance = serverLevel.getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(MissileTrackerWorld::new, MissileTrackerWorld::load),
                DATA_SAVE_ID
            );

            dimToHandlerMap.put(world.dimension(), instance);
            return instance;
        }
        else
        {
            return dimToHandlerMap.getOrDefault(world.dimension(), null);
        }
    }

    @SubscribeEvent
    public static void onWorldLoad(LevelEvent.Load event)
    {
        if (event.getLevel() instanceof Level level && !level.isClientSide())
        {
            getOrCreateHandler(level, true); // load handlers, to make missiles continue their flight
        }
    }

    @SubscribeEvent
    public static void onWorldUnload(LevelEvent.Unload event) // used to destroy existing handlers on unload
    {
        if (event.getLevel() instanceof Level level && !level.isClientSide())
        {
            MissileTrackerWorld handler = getOrCreateHandler(level, false);
            if (handler != null)
            {
                handler.destroy();
                dimToHandlerMap.remove(level.dimension());
            }
        }
    }

    // PORT: 1.12 WorldTickEvent fired for both START and END phases without a phase check, both are kept to preserve simulation speed
    @SubscribeEvent
    public static void onWorldTickPre(LevelTickEvent.Pre event)
    {
        onWorldTick(event.getLevel());
    }

    @SubscribeEvent
    public static void onWorldTickPost(LevelTickEvent.Post event)
    {
        onWorldTick(event.getLevel());
    }

    private static void onWorldTick(Level world)
    {
        if (!world.isClientSide())
        {
            MissileTrackerWorld handler = getOrCreateHandler(world, false);
            if (handler != null)
            {
                handler.onWorldTick(world);
            }
        }
    }

    @EventBusSubscriber(modid = ICBMConstants.DOMAIN, bus = EventBusSubscriber.Bus.MOD)
    public static class ModBusEvents
    {
        @SubscribeEvent
        public static void registerTicketControllers(RegisterTicketControllersEvent event)
        {
            event.register(CHUNK_TICKETS);
        }
    }
}
// PORTED
