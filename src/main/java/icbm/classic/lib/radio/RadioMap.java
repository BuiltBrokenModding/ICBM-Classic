package icbm.classic.lib.radio;

import icbm.classic.api.tile.IRadioWaveReceiver;
import icbm.classic.api.tile.IRadioWaveSender;
import icbm.classic.lib.transform.region.Cube;
import com.google.common.collect.Lists;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.ChunkPos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Map of radio wave receivers
 *
 *
 * Created by Dark(DarkGuardsman, Robert) on 3/5/2016.
 */
public class RadioMap
{
    /** DIM ID, never change */
    protected final int dimID;

    /** Map of chunk positions to receive, mainly used by short range radio gear */
    protected HashMap<ChunkPos, List<IRadioWaveReceiver>> chunk_to_entities = new HashMap();
    /** Map of receive to chunks covered */
    protected HashMap<IRadioWaveReceiver, List<ChunkPos>> receive_to_chunks = new HashMap();
    /** Cache of receivers to their range, used by {@link #update(IRadioWaveReceiver)} method */
    protected HashMap<IRadioWaveReceiver, Cube> receive_to_range = new HashMap();
    /** Cache of active senders to receivers, reduced CPU time at cost of a little memory */
    protected HashMap<IRadioWaveSender, List<IRadioWaveReceiver>> sender_to_receivers_cache = new HashMap();

    protected List<IRadioWaveReceiver> fullMapRangeReceives = new ArrayList();


    /**
     * Dimension ID
     *
     * @param dimID - unique dimension that is not already tracked
     */
    public RadioMap(int dimID)
    {
        this.dimID = dimID;
    }

    public boolean add(IRadioWaveReceiver receiver)
    {
        Cube range = receiver.getRadioReceiverRange();
        if (range != null)
        {
            if (range == RadioRegistry.INFINITE)
            {
                if (!fullMapRangeReceives.contains(receiver))
                {
                    fullMapRangeReceives.add(receiver);
                }
                return true;
            }
            if (!receive_to_chunks.containsKey(receiver))
            {
                updateChunkCache(receiver, range);
                updateSenderCache(receiver, range);
                return true;
            }
        }

        return false;
    }

    protected void updateChunkCache(IRadioWaveReceiver receiver, Cube range)
    {
        List<ChunkPos> list = range.getChunkCoords();

        //Update chunk position map
        for (ChunkPos pair : list)
        {
            List<IRadioWaveReceiver> receivers = chunk_to_entities.get(pair);
            if (receivers == null)
            {
                receivers = new ArrayList();
            }
            if (!receivers.contains(receiver))
            {
                receivers.add(receiver);
            }
            chunk_to_entities.put(pair, receivers);
        }

        //Update receiver map
        receive_to_chunks.put(receiver, list);
    }

    protected void updateSenderCache(IRadioWaveReceiver receiver, Cube range)
    {
        if (range != null)
        {
            //Update sender cache
            for (IRadioWaveSender sender : sender_to_receivers_cache.keySet())
            {
                Cube senderRange = sender.getRadioSenderRange();
                if (senderRange.doesOverlap(range))
                {
                    List<IRadioWaveReceiver> receivers = sender_to_receivers_cache.get(sender);
                    if (receivers == null)
                    {
                        receivers = new ArrayList();
                    }
                    if (!receivers.contains(receiver))
                    {
                        receivers.add(receiver);
                    }
                    sender_to_receivers_cache.put(sender, receivers);
                }
            }
        }
    }

    /**
     * Called to update data about the receiver. Used
     * when the receiver's range changes.
     *
     * @param receiver
     */
    public void update(IRadioWaveReceiver receiver)
    {
        if (!receive_to_range.containsKey(receiver) || receive_to_range.get(receiver) == null || receive_to_range.get(receiver).equals(receiver.getRadioReceiverRange()))
        {
            Cube range = receiver.getRadioReceiverRange();
            if (range != null)
            {
                updateChunkCache(receiver, range);
                updateSenderCache(receiver, range);
            }
        }
    }

    public boolean remove(IRadioWaveReceiver receiver)
    {
        if(fullMapRangeReceives.contains(receiver))
        {
            fullMapRangeReceives.remove(receiver);
        }
        if (receive_to_chunks.containsKey(receiver))
        {
            //Clear cached chunk positions
            for (ChunkPos pair : receive_to_chunks.get(receiver))
            {
                if (chunk_to_entities.containsKey(pair))
                {
                    chunk_to_entities.get(pair).remove(receiver);
                }
            }
            //Clear entry in receiver map
            receive_to_chunks.remove(receiver);

            //Update sender cache
            for (IRadioWaveSender sender : sender_to_receivers_cache.keySet())
            {
                List<IRadioWaveReceiver> receivers = sender_to_receivers_cache.get(sender);
                if (receivers == null)
                {
                    receivers = new ArrayList();
                }
                if (receivers.contains(receiver))
                {
                    receivers.remove(receiver);
                }
                sender_to_receivers_cache.put(sender, receivers);
            }
            return true;
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
    public void popMessage(IRadioWaveSender sender, float hz, String header, Object[] data)
    {
        //Cache for senders that know they will be active
        if (sender_to_receivers_cache.containsKey(sender))
        {
            for (IRadioWaveReceiver receiver : sender_to_receivers_cache.get(sender))
            {
                receiver.receiveRadioWave(hz, sender, header, data);
            }
            return;
        }

        //Receivers that have full map range, used for legacy systems mainly
        for(int i = fullMapRangeReceives.size() - 1; i >= 0; i--)
        {
            fullMapRangeReceives.get(i).receiveRadioWave(hz, sender, header, data);
        }

        //Slow way to update receives with messages
        Cube range = sender.getRadioSenderRange();
        if (range != null)
        {
            //Use simpler method if the range of number of entries is small
            if (receive_to_chunks.size() < 200 || range.getSizeX() > 320 || range.getSizeY() > 320) //20 chunks
            {
                for (IRadioWaveReceiver receiver : receive_to_chunks.keySet())
                {
                    if (receiver != null && receiver != sender)
                    {
                        Cube receiverRange = receiver.getRadioReceiverRange();
                        if (range.doesOverlap(receiverRange) || receiverRange.doesOverlap(range))
                        {
                            receiver.receiveRadioWave(hz, sender, header, data);
                        }
                    }
                }
            }
            //Complex method only used if number of receive is very high, e.g. is faster~ish than the above method
            else
            {
                List<ChunkPos> coords = range.getChunkCoords();
                List<IRadioWaveReceiver> receivers = new ArrayList();
                for (ChunkPos pair : coords)
                {
                    List<IRadioWaveReceiver> l = chunk_to_entities.get(pair);
                    if (l != null && l.size() > 0)
                    {
                        for (IRadioWaveReceiver r : l)
                        {
                            if (r != null && r != sender && !receivers.contains(r))
                            {
                                receivers.add(r);
                            }
                        }
                    }
                }
                for (IRadioWaveReceiver receiver : receivers)
                {
                    receiver.receiveRadioWave(hz, sender, header, data);
                }
            }
        }
    }

    /**
     * Updates the list of receivers for this sender.
     * <p>
     * Make sure if you call this you clear your
     * cache when the sender is no longer valid
     * use {@link TileEntity#invalidate()}
     * or {@link Entity#setDead()} to
     * clear the cache.
     * <p>
     * As well make sure to call this method
     * every so often. As the cache is not maintained
     * often or quickly. It is only updated
     * when receives are added or removed. So
     * if the sender's range changes this method
     * needs to be called.
     * <p>
     * Make sure to not call this method often
     * as its a high CPU task.
     *
     * @param sender - object that needs to
     *               have receivers cache. Needs
     *               to have a valid {@link IRadioWaveSender#getRadioSenderRange()}
     *               value in order to be cached.
     */
    public void updateCache(IRadioWaveSender sender)
    {
        //Clear cache value
        sender_to_receivers_cache.remove(sender);

        Cube range = sender.getRadioSenderRange();
        if (range != null)
        {
            sender_to_receivers_cache.put(sender, getReceiversInRange(range, sender instanceof IRadioWaveReceiver ? (IRadioWaveReceiver) sender : (IRadioWaveReceiver) null));
        }
    }

    /**
     * Gets a list of all receivers in the range
     *
     * @param range   - range to check inside
     * @param exclude - object to ignore
     * @return list of receivers, or empty list
     */
    public List<IRadioWaveReceiver> getReceiversInRange(Cube range, IRadioWaveReceiver exclude)
    {
        return getReceiversInRange(range, exclude != null ? Lists.newArrayList(exclude) : null);
    }

    /**
     * Gets a list of all receivers in the range
     *
     * @param range       - range to check inside
     * @param excludeList - tiles to ignore
     * @return list of receivers, or empty list
     */
    public List<IRadioWaveReceiver> getReceiversInRange(Cube range, List excludeList)
    {
        List<IRadioWaveReceiver> receivers = new ArrayList();
        receivers.addAll(fullMapRangeReceives);
        if (range != null)
        {
            for (IRadioWaveReceiver receiver : receive_to_chunks.keySet())
            {
                if (receiver != null && (excludeList == null || !excludeList.contains(receiver)))
                {
                    Cube receiverRange = receiver.getRadioReceiverRange();
                    if (receiverRange != null && range.doesOverlap(receiverRange))
                    {
                        receivers.add(receiver);
                    }
                }
            }
        }
        return receivers;
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
        else if (object instanceof RadioMap)
        {
            return ((RadioMap) object).dimID == dimID;
        }
        return false;
    }

    @Override
    public String toString()
    {
        return "RadioMap[" + dimID + "]";
    }

}
