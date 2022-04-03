package icbm.classic.api.explosion;

import icbm.classic.api.explosion.responses.BlastResponse;

/**
 * Created by Dark(DarkGuardsman, Robert) on 1/3/19.
 */
public enum BlastState
{
    /**
     * Triggered in main thread
     */
    TRIGGERED(true),

    /**
     * Triggered on the client
     */
    TRIGGERED_CLIENT(true),

    /**
     * Triggered in worker thread
     */
    THREADING(true),
    /**
     * Forge TNT event canceled blast
     */
    CANCLED(false),
    /**
     * Unexpected error
     */
    ERROR(false),
    /**
     * Blast was already triggered
     */
    ALREADY_TRIGGERED(true);

    public final boolean good;
    public final BlastResponse genericResponse;

    BlastState(boolean good)
    {
        this.good = good;
        this.genericResponse = new BlastResponse(this, null);
    }
}
