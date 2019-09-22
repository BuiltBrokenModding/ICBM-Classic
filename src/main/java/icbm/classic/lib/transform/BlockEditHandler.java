package icbm.classic.lib.transform;

import icbm.classic.ICBMClassic;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.function.Consumer;

/**
 * Created by Dark(DarkGuardsman, Robert) on 10/8/2018.
 */
@Mod.EventBusSubscriber(modid = ICBMClassic.DOMAIN)
public class BlockEditHandler
{
    public static final HashMap<Integer, Queue<EditQueue>> worldToRemoveQueue = new HashMap<>();
    public static float maxTickTimePercentage = 0.7f; // fraction of ticktime we are allowed to use.
    // (0.5 = 50% means that up to 50% of the 50ms that a tick may take, will be used at most)

    @SubscribeEvent
    public static void onWorldUnload(WorldEvent.Unload event)
    {
        final int dim = event.getWorld().provider.getDimension();
        if (worldToRemoveQueue.containsKey(dim))
        {
            worldToRemoveQueue.remove(dim);
        }
    }

    @SubscribeEvent
    public static void onWorldTick(TickEvent.WorldTickEvent event)
    {
        final int dim = event.world.provider.getDimension();
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

    public static void queue(World world, Collection<BlockPos> edits, Consumer<BlockPos> onEditBlock)
    {
        queue(world, edits, onEditBlock, null);
    }

    public static void queue(World world, Collection<BlockPos> edits, Consumer<BlockPos> onEditBlock, Runnable onCompleteCallback)
    {
        final int dim = world.provider.getDimension();
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
