package icbm.classic.api.explosion;

/**
 * Created by Dark(DarkGuardsman, Robert) on 1/3/19.
 */
public enum BlastState
{
    /** Triggered in main thread */
    TRIGGERED,
    /** Triggered in worker thread */
    THREADING,
    /** Forge TNT event canceled blast */
    FORGE_EVENT_CANCEL,
    /** No blast to trigger */
    NULL
}
