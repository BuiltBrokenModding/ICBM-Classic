package icbm.classic.lib.radio;

import icbm.classic.api.tile.IRadioWaveReceiver;
import icbm.classic.api.tile.IRadioWaveSender;
import icbm.classic.ICBMClassic;
import icbm.classic.lib.transform.region.Cube;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.world.World;
import net.minecraftforge.event.world.WorldEvent;

import java.util.HashMap;

/**
 * Map based system for radio waves being transmitted in an area
 *
 *
 * Created by Dark(DarkGuardsman, Robert) on 4/20/2016.
 */
public final class RadioRegistry
{
    /** Used only for event calls */
    public static final RadioRegistry INSTANCE = new RadioRegistry();

    /** Used to indicate the object has full map radio range */
    public static final Cube INFINITE = new Cube(-Integer.MAX_VALUE, -Integer.MAX_VALUE, -Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);

    /** World id to radio maps */
    private static final HashMap<Integer, RadioMap> RADIO_MAPS = new HashMap();


    /**
     * Adds an entity to the map
     *
     * @param tile - entity
     * @return true if added
     */
    public static boolean add(IRadioWaveReceiver tile)
    {
        return getRadarMapForDim(tile.world().provider.getDimension()).add(tile);
    }

    public static boolean addOrUpdate(IRadioWaveReceiver receiver)
    {
        if (!add(receiver))
        {
            RadioMap map = getRadarMapForDim(receiver.world().provider.getDimension());
            if (map.receive_to_chunks.containsKey(receiver))
            {
                map.update(receiver);
                return true;
            }
            return false;
        }
        return true;
    }

    /**
     * Removes an entity from the map
     *
     * @param tile - entity
     * @return true if removed
     */
    public static boolean remove(IRadioWaveReceiver tile)
    {
        if (RADIO_MAPS.containsKey(tile.world().provider.getDimension()))
        {
            RadioMap map = getRadarMapForDim(tile.world().provider.getDimension());
            return map.remove(tile);
        }
        return false;
    }

    /**
     * Called to send a message over the network
     *
     * @param sender - object that sent the message
     * @param hz     - frequency of the message
     * @param header - descriptive header of the message, mainly an ID system
     * @param data   - data being sent in the message
     */
    public static void popMessage(World world, IRadioWaveSender sender, float hz, String header, Object... data)
    {
        if (RADIO_MAPS.containsKey(world.provider.getDimension()))
        {
            RadioMap map = getRadarMapForDim(world.provider.getDimension());
            map.popMessage(sender, hz, header, data);
        }
    }

    /**
     * Gets a radar map for the world
     *
     * @param world - should be a valid world that is loaded and has a dim id
     * @return existing map, or new map if one does not exist
     */
    public static RadioMap getRadioMapForWorld(World world)
    {
        if (world != null && world.provider != null)
        {
            if (world.isRemote)
            {
                if (ICBMClassic.runningAsDev)
                {
                    ICBMClassic.logger().error("RadarRegistry: Radar data can not be requested client side.", new RuntimeException());
                }
                return null;
            }
            return getRadarMapForDim(world.provider.getDimension());
        }
        //Only throw an error in dev mode, ignore in normal runtime
        else if (ICBMClassic.runningAsDev)
        {
            ICBMClassic.logger().error("RadarRegistry: World can not be null or have a null provider when requesting a radar map", new RuntimeException());
        }
        return null;
    }

    /**
     * Gets a radio map for a dimension
     *
     * @param dimID - unique dim id
     * @return existing mpa, or new map if one does not exist
     */
    public static RadioMap getRadarMapForDim(int dimID)
    {
        if (!RADIO_MAPS.containsKey(dimID))
        {
            RadioMap map = new RadioMap(dimID);
            RADIO_MAPS.put(dimID, map);
            return map;
        }
        return RADIO_MAPS.get(dimID);
    }

    @SubscribeEvent
    public void worldUnload(WorldEvent.Unload event)
    {
        if (event.getWorld().provider != null)
        {
            int dim = event.getWorld().provider.getDimension();
            if (RADIO_MAPS.containsKey(dim))
            {
                getRadarMapForDim(dim).unloadAll();
                RADIO_MAPS.remove(dim);
            }
        }
    }
}
