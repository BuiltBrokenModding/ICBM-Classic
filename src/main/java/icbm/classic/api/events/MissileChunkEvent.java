package icbm.classic.api.events;

import icbm.classic.content.missile.tracker.LoadedChunkPair;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.bus.api.Event;
import net.neoforged.neoforge.common.world.chunk.TicketController;

public class MissileChunkEvent extends Event
{
    public final LoadedChunkPair pair;
    /** PORT: 1.12 ForgeChunkManager.Ticket replaced by the NeoForge ticket controller used to force the chunk */
    public final TicketController ticket;

    public MissileChunkEvent(LoadedChunkPair pair, TicketController ticket)
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
    public static class Load extends MissileChunkEvent implements ICancellableEvent
    {
        public Load(LoadedChunkPair pair, TicketController ticket)
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
    public static class Unload extends MissileChunkEvent implements ICancellableEvent
    {
        public Unload(LoadedChunkPair pair, TicketController ticket)
        {
            super(pair, ticket);
        }
    }
}
// PORTED
