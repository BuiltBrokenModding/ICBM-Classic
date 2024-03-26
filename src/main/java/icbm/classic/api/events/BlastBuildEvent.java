package icbm.classic.api.events;

import icbm.classic.api.explosion.IBlastInit;

/**
 * Fired when a blast is built to allow changing settings before the blast is locked into its settings.
 * <p>
 * <p>
 * Created by Dark(DarkGuardsman, Robert) on 1/3/19.
 */
public class BlastBuildEvent extends BlastEvent<IBlastInit> {
    public BlastBuildEvent(IBlastInit blast) {
        super(blast);
    }

    /**
     * Casts this event's blast to {@link IBlastInit} to allow changing its values
     *
     * @return The blast init
     */
    public IBlastInit getBlastInit() {
        return blast;
    }
}
