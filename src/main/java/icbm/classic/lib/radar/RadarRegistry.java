package icbm.classic.lib.radar;

import com.google.common.collect.Lists;
import icbm.classic.ICBMClassic;
import icbm.classic.lib.transform.region.Cube;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;

import java.util.HashMap;
import java.util.List;

/**
 * Map based system for tracking objects using a radar devices. Only works server side to prevent unwanted data from stacking up.
 *
 *
 * Created by Dark(DarkGuardsman, Robin) on 3/5/2016.
 */
public final class RadarRegistry
{
    /** Used only for event calls */
    public static final RadarRegistry INSTANCE = new RadarRegistry();
    //TODO add client side version for mini-map like systems
    //TODO add per machine tracking map that uses line of sight so hills can block it's view. (Visible Area Cache in other words)
    /** Level id to radar map */
    private static final HashMap<ResourceKey<Level>, RadarMap> RADAR_MAPS = new HashMap();

    /**
     * Adds an entity to the radar map
     *
     * @param entity - entity
     * @return true if added
     */
    public static boolean add(Entity entity)
    {
        if (entity != null && !entity.isRemoved() && entity.level() != null && !entity.level().isClientSide())
        {
            RadarMap map = getRadarMapForWorld(entity.level());
            return map != null && getRadarMapForWorld(entity.level()).add(entity);
        }
        return false;
    }

    /**
     * Removes an entity from the radar map
     *
     * @param entity - entity
     * @return true if removed
     */
    public static boolean remove(Entity entity)
    {
        if (entity != null && !entity.isRemoved() && entity.level() != null)
        {
            RadarMap map = getRadarMapForWorld(entity.level());
            return map != null ? getRadarMapForWorld(entity.level()).remove(entity) : false;
        }
        return false;
    }

    /**
     * Gets a radar map for the world
     *
     * @param world - should be a valid world that is loaded and has a dim id
     * @return existing map, or new map if one does not exist
     */
    public static RadarMap getRadarMapForWorld(Level world)
    {
        if (world != null)
        {
            if (world.isClientSide())
            {
                if (ICBMClassic.runningAsDev)
                {
                    ICBMClassic.logger().error("RadarRegistry: Radar data can not be requested client side.", new RuntimeException());
                }
                return null;
            }
            return getRadarMapForDim(world.dimension());
        }
        //Only throw an error in dev mode, ignore in normal runtime
        else if (ICBMClassic.runningAsDev)
        {
            ICBMClassic.logger().error("RadarRegistry: Level can not be null or have a null provider when requesting a radar map", new RuntimeException());
        }
        return null;
    }

    /**
     * Gets a radar map for a dimension
     *
     * @param dimID - dimension key
     * @return existing mpa, or new map if one does not exist
     */
    public static RadarMap getRadarMapForDim(ResourceKey<Level> dimID)
    {
        if (!RADAR_MAPS.containsKey(dimID))
        {
            RadarMap map = new RadarMap(dimID);
            RADAR_MAPS.put(dimID, map);
            return map;
        }
        return RADAR_MAPS.get(dimID);
    }

    /**
     * Grabs all living radar objects within range 2D
     *
     * @param world to check
     * @param x center
     * @param y center
     * @param z center
     * @param distance in x & z axis
     * @return list, never null
     */
    public static List<Entity> getAllLivingObjectsWithin(Level world, double x, double y, double z, double distance)
    {
        return getAllLivingObjectsWithin(world, new Cube(x - distance, 0, z - distance, x + distance, ICBMClassic.MAP_HEIGHT, z + distance));
    }

    /**
     * Grabs all living radar objects within range
     *
     * @param world
     * @param cube  - area to search for contacts
     * @return list, never null
     */
    public static List<Entity> getAllLivingObjectsWithin(Level world, Cube cube)
    {
        // TODO recode to take filter as input to reduce output list
        // TODO recode to use a consumer pattern, if so ignore filter and let consumer be the filter

        final List<Entity> list = Lists.<Entity>newArrayList();
        if (RADAR_MAPS.containsKey(world.dimension()))
        {
            final RadarMap map = getRadarMapForWorld(world);
            if (map != null)
            {
                final List<RadarEntity> objects = map.getRadarObjects(cube, true);
                for (RadarEntity object : objects)
                {
                    if (object != null && object.isValid())
                    {
                        Entity entity = ((RadarEntity) object).entity;
                        if (entity != null && !entity.isRemoved())
                        {
                            list.add(entity);
                        }
                    }
                }
            }
            else if (world.isClientSide() && ICBMClassic.runningAsDev)
            {
                ICBMClassic.logger().error("RadarRegistry: Radar data can not be requested client side.", new RuntimeException());
            }
        }
        return list;
    }

    // PORT: LOWEST so MissileEventHandler#chunkUnload (NORMAL) reads the chunk's contacts before they are dropped.
    // In 1.12 the missile handler was registered first; here RadarRegistry is registered in the mod constructor,
    // before FML injects @EventBusSubscriber classes, so same-priority ordering would be reversed
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void chunkUnload(ChunkEvent.Unload event)
    {
        if (event.getLevel() instanceof Level level && !level.isClientSide()) // PORT: server maps only, client levels share dimension keys
        {
            final ResourceKey<Level> dim = level.dimension();
            if (RADAR_MAPS.containsKey(dim))
            {
                getRadarMapForDim(dim).remove(event.getChunk());
            }
        }
    }

    @SubscribeEvent
    public void worldUpdateTick(LevelTickEvent.Post event)
    {
        final Level level = event.getLevel();
        if (!level.isClientSide())
        {
            final ResourceKey<Level> dim = level.dimension();
            if (RADAR_MAPS.containsKey(dim))
            {
                RadarMap map = getRadarMapForDim(dim);
                if (map.isEmpty())
                {
                    RADAR_MAPS.remove(dim);
                }
                else
                {
                    map.update();
                }
            }
        }
    }

    @SubscribeEvent
    public void worldUnload(LevelEvent.Unload event)
    {
        if (event.getLevel() instanceof Level level && !level.isClientSide()) // PORT: server maps only, client levels share dimension keys
        {
            final ResourceKey<Level> dim = level.dimension();
            if (RADAR_MAPS.containsKey(dim))
            {
                getRadarMapForDim(dim).unloadAll();
                RADAR_MAPS.remove(dim);
            }
        }
    }

}
// PORTED
