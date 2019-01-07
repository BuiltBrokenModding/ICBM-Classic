package icbm.classic.api.events;

import icbm.classic.api.explosion.IBlastInit;

/**
 * Fired when a blast is built to allow changing settings before the blast is locked into its settings.
 *
 *
 * Created by Dark(DarkGuardsman, Robert) on 1/3/19.
 */
public class BlastBuildEvent extends BlastEvent<IBlastInit>
{
    public BlastBuildEvent(IBlastInit blast)
    {
        super(blast);
    }
}
