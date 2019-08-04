package icbm.classic.lib.radar;

import icbm.classic.ICBMClassic;
import icbm.classic.lib.transform.region.Cube;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.Chunk;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * System designed to track moving or stationary targets on a 2D map. Can be used to detect objects or visualize objects in an area. Mainly
 * used to track flying objects that are outside of the map bounds(Missile in ICBM).
 * <p>
 * <p>
 * Created by Dark(DarkGuardsman, Robert) on 3/5/2016.
 */
public class RadarMap
{
    public static final int UPDATE_DELAY = 20;

    /** DIM ID, never change */
    public final int dimID;

    /** Map of chunk coords( converted to long) to radar contacts in that chunk */
    public final HashMap<ChunkPos, List<RadarEntity>> chunk_to_entities = new HashMap();
    public final List<RadarEntity> allEntities = new ArrayList();

    public int ticks = 0;

    /**
     * Dimension ID
     *
     * @param dimID - unique dimension that is not already tracked
     */
    public RadarMap(int dimID)
    {
        this.dimID = dimID;
    }

    /**
     * Called at the end of every world tick to do checks on
     * data stored.
     */
    public void update()
    {
        if (ticks++ >= UPDATE_DELAY && chunk_to_entities.size() > 0)
        {
            ticks = 0;
            //TODO consider multi-threading if number of entries is too high (need to ensure runs in less than 10ms~)


            HashMap<RadarEntity, ChunkPos> removeList = new HashMap();
            List<RadarEntity> addList = new ArrayList();
            for (Map.Entry<ChunkPos, List<RadarEntity>> entry : chunk_to_entities.entrySet())
            {
                if (entry.getValue() != null)
                {
                    for (RadarEntity object : entry.getValue())
                    {
                        if (entry.getKey() != object.getChunkPos())
                        {
                            removeList.put(object, entry.getKey());
                            if (object.isValid())
                            {
                                addList.add(object);
                            }
                        }
                    }
                }
            }

            for (Map.Entry<RadarEntity, ChunkPos> entry : removeList.entrySet())
            {
                allEntities.remove(entry.getKey());
                List<RadarEntity> list = chunk_to_entities.get(entry.getValue());
                if (list != null)
                {
                    list.remove(entry.getKey());
                    if (list.size() > 0)
                    {
                        chunk_to_entities.put(entry.getValue(), list);
                    }
                    else
                    {
                        chunk_to_entities.remove(entry.getValue());
                    }
                }
                else
                {
                    chunk_to_entities.remove(entry.getValue());
                }
            }

            addList.forEach(this::add);

            Iterator<RadarEntity> it = allEntities.iterator();
            while (it.hasNext())
            {
                RadarEntity object = it.next();
                if (!object.isValid())
                {
                    it.remove();
                }
            }
        }
    }

    public boolean add(Entity entity)
    {
        return add(new RadarEntity(entity));
    }

    public boolean add(RadarEntity object)
    {
        if (!allEntities.contains(object) && object.isValid())
        {
            allEntities.add(object);
            ChunkPos pair = getChunkValue((int) object.x(), (int) object.z());
            List<RadarEntity> list;

            //Get list or make new
            if (chunk_to_entities.containsKey(pair))
            {
                list = chunk_to_entities.get(pair);
            }
            else
            {
                list = new ArrayList();
            }

            //Check if object is not already added
            if (!list.contains(object))
            {
                list.add(object);
                //TODO fire map update event
                //TODO fire map add event
                //Update map
                chunk_to_entities.put(pair, list);
                return true;
            }
        }
        return false;
    }

    public boolean remove(Entity entity)
    {
        return remove(new RadarEntity(entity));
    }

    public boolean remove(RadarEntity object)
    {
        ChunkPos pair = getChunkValue((int) object.x(), (int) object.z());
        allEntities.remove(object);
        if (chunk_to_entities.containsKey(pair))
        {
            List<RadarEntity> list = chunk_to_entities.get(pair);
            boolean b = list.remove(object);
            //TODO fire radar remove event
            //TODO fire map update event
            if (list.isEmpty())
            {
                chunk_to_entities.remove(pair);
            }
            return b;
        }
        return false;
    }

    /**
     * Removes all entries connected with the provided chunk location data
     *
     * @param chunk - should never be null
     */
    public void remove(Chunk chunk)
    {
        ChunkPos pair = chunk.getPos();
        if (chunk_to_entities.containsKey(pair))
        {
            for (RadarEntity object : chunk_to_entities.get(pair))
            {
                //TODO fire remove event
                allEntities.remove(object);
            }
            chunk_to_entities.remove(pair);
        }
    }

    protected final ChunkPos getChunkValue(int x, int z)
    {
        return new ChunkPos(x >> 4, z >> 4);
    }

    public void unloadAll()
    {
        chunk_to_entities.clear();
    }

    /**
     * Finds all contacts within chunk distances
     *
     * @param x        - world location x
     * @param z        - world location x
     * @param distance - distance m
     * @return list of entries
     */
    public List<RadarEntity> getRadarObjects(double x, double z, double distance)
    {
        return getRadarObjects(new Cube(x - distance, 0, z - distance, x + distance, ICBMClassic.MAP_HEIGHT, z + distance).cropToWorld(), true);
    }

    public List<RadarEntity> getEntitiesInChunk(int chunkX, int chunkZ)
    {
        ChunkPos p = new ChunkPos(chunkX, chunkZ);
        return chunk_to_entities.get(p);
    }

    public void collectEntitiesInChunk(int chunkX, int chunkZ, Consumer<RadarEntity> collector)
    {
        final List<RadarEntity> objects = getEntitiesInChunk(chunkX, chunkZ);
        if (objects != null)
        {
            for (RadarEntity entity : objects)
            {
                if (entity != null && entity.isValid())
                {
                    collector.accept(entity);
                }
            }
        }
    }

    /**
     * Finds all contacts within chunk distances
     *
     * @param cube  - area to search inside, approximated to chunk bounds
     * @param exact - match exact cube size, overrides approximation
     * @return list of entries
     */
    public List<RadarEntity> getRadarObjects(Cube cube, boolean exact)
    {
        final List<RadarEntity> list = new ArrayList();
        for (int chunkX = (cube.min().xi() >> 4) - 1; chunkX <= (cube.max().xi() >> 4) + 1; chunkX++)
        {
            for (int chunkZ = (cube.min().zi() >> 4) - 1; chunkZ <= (cube.max().zi() >> 4) + 1; chunkZ++)
            {
                collectEntitiesInChunk(chunkX, chunkZ, (entity) -> {
                    if (exact || exact && cube != null && cube.isWithin(entity))
                    {
                        list.add(entity);
                    }
                });
            }
        }
        return list;
    }

    /**
     * Dimension ID this map tracks
     *
     * @return valid dim ID.
     */
    public int dimID()
    {
        return dimID;
    }

    @Override
    public boolean equals(Object object)
    {
        if (object == this)
        {
            return true;
        }
        else if (object instanceof RadarMap)
        {
            return ((RadarMap) object).dimID == dimID;
        }
        return false;
    }

    @Override
    public String toString()
    {
        return "RadarMap[" + dimID + "]";
    }


}
