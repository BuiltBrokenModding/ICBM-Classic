package icbm.classic.api.events;

import icbm.classic.content.entity.missile.tracker.LoadedChunkPair;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.ForgeChunkManager.Ticket;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

public class MissileChunkEvent extends Event
{
    public final LoadedChunkPair pair;
    public final ForgeChunkManager.Ticket ticket;

    public MissileChunkEvent(LoadedChunkPair pair, ForgeChunkManager.Ticket ticket)
    {
        this.pair = pair;
        this.ticket = ticket;
    }

    /**
     * Called when a missile is about to load a chunk.
     * Use this to change the amount of time the cunk
     * should be loaded for, or cancel this event
     * to not load the chunk.
     */
    @Cancelable
    public static class Load extends MissileChunkEvent
    {
        public Load(LoadedChunkPair pair, Ticket ticket)
        {
            super(pair, ticket);
        }
    }

    /**
     * Called when a chunk loaded by a missile is about
     * to be unloaded. Cancel this event to not unload the
     * chunk. Make sure to change the time the chunk should
     * be loaded for or another attempt to unload the chunk
     * will be made in the next world tick.
     */
    @Cancelable
    public static class Unload extends MissileChunkEvent
    {
        public Unload(LoadedChunkPair pair, Ticket ticket)
        {
            super(pair, ticket);
        }
    }
}
