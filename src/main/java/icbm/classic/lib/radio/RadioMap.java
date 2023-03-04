package icbm.classic.lib.radio;

import icbm.classic.ICBMClassic;
import icbm.classic.api.data.IBoundBox;
import icbm.classic.api.radio.IRadio;
import icbm.classic.api.radio.IRadioMessage;
import icbm.classic.api.radio.IRadioReceiver;
import icbm.classic.api.radio.IRadioSender;
import com.google.common.collect.Lists;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
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
    protected HashMap<ChunkPos, List<IRadioReceiver>> chunk_to_entities = new HashMap();
    /** Map of receive to chunks covered, only for static tiles */
    protected HashMap<IRadioReceiver, List<ChunkPos>> receive_to_chunks = new HashMap();
    /** Map of sender to receives, only for static tiles */
    protected HashMap<IRadioSender, List<IRadioReceiver>> sender_to_receivers_cache = new HashMap();
    /** Cache of receivers to their range, used by {@link #update(IRadioReceiver)} method */
    protected HashMap<IRadioReceiver, IBoundBox<BlockPos>> receive_to_range = new HashMap();

    protected List<IRadioReceiver> fullMapRangeReceives = new ArrayList();


    /**
     * Dimension ID
     *
     * @param dimID - unique dimension that is not already tracked
     */
    public RadioMap(int dimID)
    {
        this.dimID = dimID;
    }

    public boolean add(IRadioReceiver receiver)
    {
        if(receiver.getWorld() == null || receiver.getWorld().isRemote)
        {
            return false;
        }
        final IBoundBox<BlockPos> range = receiver.getRange();
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

    public static List<ChunkPos> getChunkCoords(IBoundBox<BlockPos> range)
    {
        List<ChunkPos> chunks = new ArrayList();
        for (int chunkX = (range.lowerBound().getX() >> 4) - 1; chunkX <= (range.upperBound().getX() >> 4) + 1; chunkX++)
        {
            for (int chunkZ = (range.lowerBound().getZ() >> 4) - 1; chunkZ <= (range.upperBound().getZ() >> 4) + 1; chunkZ++)
            {
                chunks.add(new ChunkPos(chunkX, chunkZ));
            }
        }
        return chunks;
    }

    public static boolean doesOverlap(IBoundBox<BlockPos> self, IBoundBox<BlockPos> box)
    {
        return !isOutSide(box.lowerBound().getX(), box.upperBound().getX(), self.lowerBound().getX(), self.upperBound().getX())
            || !isOutSide(box.lowerBound().getY(), box.upperBound().getY(), self.lowerBound().getY(), self.upperBound().getY())
            || !isOutSide(box.lowerBound().getZ(), box.upperBound().getZ(), self.lowerBound().getZ(), self.upperBound().getZ());
    }

    public static boolean isOutSide(int boxMin, int boxMax, int selfMin, int selfMax)
    {
        return selfMin > boxMin || boxMax > selfMax;
    }

    protected void updateChunkCache(IRadioReceiver receiver, IBoundBox<BlockPos> range)
    {
        final List<ChunkPos> list = getChunkCoords(range);

        //Update chunk position map
        for (ChunkPos pair : list)
        {
            List<IRadioReceiver> receivers = chunk_to_entities.get(pair);
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

    protected void updateSenderCache(IRadioReceiver receiver, IBoundBox<BlockPos> range)
    {
        if (range != null)
        {
            //Update sender cache
            for (IRadioSender sender : sender_to_receivers_cache.keySet())
            {
                final IBoundBox<BlockPos> senderRange = sender.getRange();
                if (doesOverlap(senderRange, range))
                {
                    List<IRadioReceiver> receivers = sender_to_receivers_cache.get(sender);
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
    public void update(IRadioReceiver receiver)
    {
        if (!receive_to_range.containsKey(receiver) || receive_to_range.get(receiver) == null || receive_to_range.get(receiver).equals(receiver.getRange()))
        {
            IBoundBox<BlockPos> range = receiver.getRange();
            if (range != null)
            {
                updateChunkCache(receiver, range);
                updateSenderCache(receiver, range);
            }
        }
    }

    public boolean remove(IRadioReceiver receiver)
    {
        if(receiver.getWorld() == null || receiver.getWorld().isRemote)
        {
            return false;
        }
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
            for (IRadioSender sender : sender_to_receivers_cache.keySet())
            {
                List<IRadioReceiver> receivers = sender_to_receivers_cache.get(sender);
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
     * @param sender pushing the message
     * @param packet containing message
     */
    public void popMessage(IRadioSender sender, IRadioMessage packet)
    {

        if(packet == null || packet.getChannel() == null || packet.getChannel().trim().isEmpty() || sender == null) {
            ICBMClassic.logger().error("RadarMap[" + dimID + "]: Invalid radio message " + sender + " " + packet, new RuntimeException());
            return;
        }

        //Cache for senders that know they will be active
        if (sender_to_receivers_cache.containsKey(sender))
        {
            for (IRadioReceiver receiver : sender_to_receivers_cache.get(sender))
            {
                receiver.onMessage(sender, packet);
            }
            return;
        }

        //Receivers that have full map range, used for legacy systems mainly
        for(int i = fullMapRangeReceives.size() - 1; i >= 0; i--)
        {
            fullMapRangeReceives.get(i).onMessage(sender, packet);
        }

        //Slow way to update receives with messages
        final IBoundBox<BlockPos> senderRange = sender.getRange();
        if (senderRange != null)
        {
            //Use simpler method if the range of number of entries is small
            if (receive_to_chunks.size() < 200 || (senderRange.upperBound().getX() - senderRange.lowerBound().getX()) > 320 || (senderRange.upperBound().getZ() - senderRange.lowerBound().getZ()) > 320) //20 chunks
            {
                for (IRadioReceiver receiver : receive_to_chunks.keySet())
                {
                    if (receiver != null && receiver != sender)
                    {
                        IBoundBox<BlockPos> receiverRange = receiver.getRange();
                        if (doesOverlap(senderRange, receiverRange) || doesOverlap(receiverRange, senderRange))
                        {
                            receiver.onMessage(sender, packet);
                        }
                    }
                }
            }
            //Complex method only used if number of receive is very high, e.g. is faster~ish than the above method
            else
            {
                final List<ChunkPos> coords = getChunkCoords(senderRange);
                final List<IRadioReceiver> receivers = new ArrayList(); //TODO move over to collector pattern
                for (ChunkPos pair : coords)
                {
                    final List<IRadioReceiver> l = chunk_to_entities.get(pair);
                    if (l != null && l.size() > 0)
                    {
                        for (IRadioReceiver r : l)
                        {
                            if (r != null && r != sender && !receivers.contains(r))
                            {
                                receivers.add(r);
                            }
                        }
                    }
                }
                for (IRadioReceiver receiver : receivers)
                {
                    receiver.onMessage(sender, packet);
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
    public void updateCache(IRadioSender sender)
    {
        //Clear cache value
        sender_to_receivers_cache.remove(sender);

        final IBoundBox<BlockPos> range = sender.getRange();
        if (range != null)
        {
            sender_to_receivers_cache.put(sender, getReceiversInRange(range, sender instanceof IRadioReceiver ? (IRadioReceiver) sender : (IRadioReceiver) null));
        }
    }

    /**
     * Gets a list of all receivers in the range
     *
     * @param range   - range to check inside
     * @param exclude - object to ignore
     * @return list of receivers, or empty list
     */
    public List<IRadioReceiver> getReceiversInRange(IBoundBox<BlockPos> range, IRadioReceiver exclude)
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
    public List<IRadioReceiver> getReceiversInRange(IBoundBox<BlockPos> range, List<IRadio> excludeList)
    {
        List<IRadioReceiver> receivers = new ArrayList();
        receivers.addAll(fullMapRangeReceives);
        if (range != null)
        {
            for (IRadioReceiver receiver : receive_to_chunks.keySet())
            {
                if (receiver != null && (excludeList == null || !excludeList.contains(receiver)))
                {
                    final IBoundBox<BlockPos> receiverRange = receiver.getRange();
                    if (receiverRange != null && doesOverlap(range, receiverRange))
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
