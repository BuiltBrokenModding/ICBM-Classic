package icbm.classic.lib.transform;

import icbm.classic.ICBMConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.function.Consumer;

/**
 * Created by Dark(DarkGuardsman, Robin) on 10/8/2018.
 */
@EventBusSubscriber(modid = ICBMConstants.DOMAIN)
public class BlockEditHandler
{
    public static final HashMap<ResourceKey<Level>, Queue<EditQueue>> worldToRemoveQueue = new HashMap<>();
    public static float maxTickTimePercentage = 0.7f; // fraction of ticktime we are allowed to use.
    // (0.5 = 50% means that up to 50% of the 50ms that a tick may take, will be used at most)

    @SubscribeEvent
    public static void onWorldUnload(LevelEvent.Unload event)
    {
        if (!(event.getLevel() instanceof Level level) || level.isClientSide())
        {
            return;
        }
        final ResourceKey<Level> dim = level.dimension();
        if (worldToRemoveQueue.containsKey(dim))
        {
            worldToRemoveQueue.remove(dim);
        }
    }

    // PORT: 1.12 WorldTickEvent fired for both START and END phases, both are kept to keep the same edit throughput.
    // Limited to the server as the queue is only filled by server side logic.
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
        if (world.isClientSide())
        {
            return;
        }
        final ResourceKey<Level> dim = world.dimension();
        if (worldToRemoveQueue.containsKey(dim))
        {
            long startTime = System.currentTimeMillis();
            long endTime = startTime + (long)(50f * maxTickTimePercentage);  // calculate time when to stop
            Queue<EditQueue> list = worldToRemoveQueue.get(dim);

            if (!list.isEmpty())
            {
                //Limit max batchsize of edits (amount of edits between checking the time)
                int limitPerQueue = 50; // TODO test this value

                //Loop queues until we run out of time
                while (System.currentTimeMillis() < endTime && !list.isEmpty())
                {
                    //Get next queue
                    EditQueue queue = list.peek();
                    if (queue != null)
                    {
                        //Do edits
                        queue.doWork(limitPerQueue);

                        //clear if done
                        if (queue.queue.isEmpty())
                        {
                            list.poll();
                        }
                    }
                    //clear if null
                    else
                    {
                        list.poll();
                    }
                }
            }
        }
    }

    public static void queue(Level world, Collection<BlockPos> edits, Consumer<BlockPos> onEditBlock)
    {
        queue(world, edits, onEditBlock, null);
    }

    public static void queue(Level world, Collection<BlockPos> edits, Consumer<BlockPos> onEditBlock, Runnable onCompleteCallback)
    {
        final ResourceKey<Level> dim = world.dimension();
        if (!worldToRemoveQueue.containsKey(dim))
        {
            worldToRemoveQueue.put(dim, new LinkedList<>());
        }
        worldToRemoveQueue.get(dim).add(new EditQueue(edits, onEditBlock, onCompleteCallback));
    }

    public static class EditQueue
    {
        final Queue<BlockPos> queue = new LinkedList<>();
        final Consumer<BlockPos> onEditBlock;
        final Runnable onCompleteCallback;

        public EditQueue(Collection<BlockPos> edits, Consumer<BlockPos> onEditBlock, Runnable onCompleteCallback)
        {
            queue.addAll(edits);
            this.onEditBlock = onEditBlock;
            this.onCompleteCallback = onCompleteCallback;
        }

        void doWork(int limit)
        {
            int editsCount = 0;
            while (editsCount <= limit && !queue.isEmpty())
            {
                onEditBlock.accept(queue.poll());
                editsCount++;
            }

            if (queue.isEmpty() && onCompleteCallback != null)
            {
                onCompleteCallback.run();
            }
        }
    }
}
// PORTED
