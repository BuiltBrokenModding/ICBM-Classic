package icbm.classic.api.explosion;

/**
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
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
