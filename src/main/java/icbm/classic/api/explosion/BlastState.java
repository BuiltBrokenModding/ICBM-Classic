package icbm.classic.api.explosion;

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
     * Triggered in worker thread
     */
    THREADING(true),
    /**
     * Forge TNT event canceled blast
     */
    FORGE_EVENT_CANCEL(false),
    /**
     * No blast to trigger
     */
    NULL(false),
    /**
     * Unexpected error
     */
    ERROR(false),
    /**
     * Blast was already triggered
     */
    ALREADY_TRIGGERED(true);

    public final boolean good;

    BlastState(boolean good)
    {
        this.good = good;
    }
}
