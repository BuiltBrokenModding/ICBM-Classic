package icbm.classic.api.missiles;

/**
 * Launcher status codes
 * Created by Dark(DarkGuardsman, Robert) on 1/9/19.
 */
public enum LaunchStatus
{
    /** Missile was fired */
    LAUNCHED(false),
    /** Missile can fire */
    CAN_LAUNCH(false),

    //Error codes
    /** Launcher is invalid */
    ERROR_INVALID(true),
    /** Min range safety was triggered */
    ERROR_MIN_RANGE(true),
    /** Max range safety was triggered */
    ERROR_MAX_RANGE(true),
    /** Missile contained safety was triggered */
    ERROR_NO_MISSILE(true),
    /** Missile type safety was triggered */
    ERROR_INVALID_MISSILE(true);

    /** True if the code is an error type */
    public final boolean isError;

    LaunchStatus(boolean isError)
    {
        this.isError = isError;
    }
}
