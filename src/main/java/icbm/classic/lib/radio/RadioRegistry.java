package icbm.classic.lib.radio;

import icbm.classic.ICBMClassic;
import icbm.classic.api.data.IBoundBox;
import icbm.classic.api.radio.IRadioMessage;
import icbm.classic.api.radio.IRadioReceiver;
import icbm.classic.api.radio.IRadioSender;
import icbm.classic.lib.data.BoundBlockPos;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.bus.api.SubscribeEvent;

import java.util.HashMap;

/**
 * Map based system for radio waves being transmitted in an area
 *
 *
 * Created by Dark(DarkGuardsman, Robin) on 4/20/2016.
 */
public final class RadioRegistry
{
    /** Used only for event calls */
    public static final RadioRegistry INSTANCE = new RadioRegistry();

    /** Used to indicate the object has full map radio range */
    public static final IBoundBox<BlockPos> INFINITE = new BoundBlockPos(-Integer.MAX_VALUE, -Integer.MAX_VALUE, -Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);

    /** Level id to radio maps */
    private static final HashMap<ResourceKey<Level>, RadioMap> RADIO_MAPS = new HashMap();

    public static String EMPTY_HZ = "";


    /**
     * Adds an entity to the map
     *
     * @param receiver - entity
     * @return true if added
     */
    public static boolean add(IRadioReceiver receiver)
    {
        if(receiver.getWorld() == null)
        {
            return false;
        }
        return getRadarMapForDim(receiver.getWorld().dimension()).add(receiver);
    }

    public static boolean addOrUpdate(IRadioReceiver receiver)
    {
        if (!add(receiver))
        {
            RadioMap map = getRadarMapForDim(receiver.getWorld().dimension());
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
    public static boolean remove(IRadioReceiver tile)
    {
        if(tile.getWorld() == null)
        {
            return false;
        }
        if (RADIO_MAPS.containsKey(tile.getWorld().dimension()))
        {
            RadioMap map = getRadarMapForDim(tile.getWorld().dimension());
            return map.remove(tile);
        }
        return false;
    }

    /**
     * Called to send a message over the network
     *
     * @param sender posting message
     * @param packet containing message
     */
    public static void popMessage(Level world, IRadioSender sender, IRadioMessage packet)
    {
        if(world == null || world.isClientSide()) {
            ICBMClassic.logger().error("RadarRegistry: Invalid world : " + world, new RuntimeException());
            return;
        }
        if (RADIO_MAPS.containsKey(world.dimension()))
        {
            RadioMap map = getRadarMapForDim(world.dimension());
            map.popMessage(sender, packet);
        }
    }

    /**
     * Gets a radar map for the world
     *
     * @param world - should be a valid world that is loaded and has a dim id
     * @return existing map, or new map if one does not exist
     */
    public static RadioMap getRadioMapForWorld(Level world)
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
     * Gets a radio map for a dimension
     *
     * @param dimID - dimension key
     * @return existing mpa, or new map if one does not exist
     */
    public static RadioMap getRadarMapForDim(ResourceKey<Level> dimID)
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
    public void worldUnload(LevelEvent.Unload event)
    {
        if (event.getLevel() instanceof Level level && !level.isClientSide()) // PORT: server maps only, client levels share dimension keys
        {
            final ResourceKey<Level> dim = level.dimension();
            if (RADIO_MAPS.containsKey(dim))
            {
                getRadarMapForDim(dim).unloadAll();
                RADIO_MAPS.remove(dim);
            }
        }
    }
}
// PORTED
