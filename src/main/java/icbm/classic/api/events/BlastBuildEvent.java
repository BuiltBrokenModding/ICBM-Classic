package icbm.classic.api.events;

import icbm.classic.api.explosion.IBlastInit;

/**
 * Fired when a blast is built to allow changing settings before the blast is locked into its settings.
 *
 *
 * Created by Dark(DarkGuardsman, Robin) on 1/3/19.
 *
 * @deprecated being replaced with {@link icbm.classic.api.actions.IAction} which will not
 * provide an event for creating. Only an event for pre-run and post-run.
 */
public class BlastBuildEvent extends BlastEvent<IBlastInit>
{
    public BlastBuildEvent(IBlastInit blast)
    {
        super(blast);
    }

    /**
     * Casts this event's blast to {@link IBlastInit} to allow changing its values
     * @return The blast init
     */
    public IBlastInit getBlastInit()
    {
        return (IBlastInit)blast;
    }
}
// PORTED
