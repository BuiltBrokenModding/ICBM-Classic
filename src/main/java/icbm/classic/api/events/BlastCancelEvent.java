package icbm.classic.api.events;

import icbm.classic.api.explosion.IBlast;
import net.minecraftforge.fml.common.eventhandler.Cancelable;

/**
 * Called when one blast causes the removal (destroys) another blast.
 * Cancel this event to not remove (destroy) the affected blast
 */
@Cancelable
public class BlastCancelEvent extends BlastEvent
{
    private IBlast canceledBlast;

    public BlastCancelEvent(IBlast blast, IBlast canceledBlast)
    {
        super(blast);
        this.canceledBlast = canceledBlast;
    }

    /**
     * @return The blast that will get removed (destroyed)
     */
    public IBlast getCanceledBlast()
    {
        return canceledBlast;
    }
}
