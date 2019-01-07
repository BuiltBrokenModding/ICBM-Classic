package icbm.classic.content.explosive.thread2;

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
 *
 * Created by Dark(DarkGuardsman, Robert) on 10/8/2018.
 */
@Mod.EventBusSubscriber(modid = ICBMClassic.DOMAIN)
public class BlockEditHandler
{
    public static final HashMap<Integer, Queue<EditQueue>> worldToRemoveQueue = new HashMap();
    public static int blockEditsPerTick = 10000;
    public static int blockEditsPerQueue = 100;

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
            int editsLeft = blockEditsPerTick;
            Queue<EditQueue> list = worldToRemoveQueue.get(dim);

            if (list.peek() != null)
            {
                //Limit max edits per queue
                int limitPerQueue = Math.max(blockEditsPerQueue, blockEditsPerTick / list.size());

                //Loop queues until we run out of block edits
                while (editsLeft > 0 && list.peek() != null)
                {
                    //Get next queue
                    EditQueue queue = list.peek();
                    if (queue != null)
                    {
                        //Do edits
                        editsLeft -= queue.doWork(limitPerQueue);

                        //clear if done
                        if (queue.isDone())
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
        final int dim = world.provider.getDimension();
        if (!worldToRemoveQueue.containsKey(dim))
        {
            worldToRemoveQueue.put(dim, new LinkedList());
        }
        worldToRemoveQueue.get(dim).add(new EditQueue(edits, onEditBlock));
    }

    public static class EditQueue
    {
        public final Queue<BlockPos> queue = new LinkedList();
        public final Consumer<BlockPos> onEditBlock;

        public EditQueue(Collection<BlockPos> edits, Consumer<BlockPos> onEditBlock)
        {
            queue.addAll(edits);
            this.onEditBlock = onEditBlock;
        }

        public int doWork(int limit)
        {
            int edits = 0;
            while (edits <= limit && !isDone())
            {
                onEditBlock.accept(queue.poll());
                edits++;
            }
            return edits;
        }

        public boolean isDone()
        {
            return queue.peek() == null;
        }
    }


}
