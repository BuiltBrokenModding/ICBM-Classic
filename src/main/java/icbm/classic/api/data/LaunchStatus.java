package icbm.classic.api.data;

/**
 * Created by Dark(DarkGuardsman, Robert) on 1/9/19.
 */
public enum LaunchStatus
{
    LAUNCHED(false),
    CAN_LAUNCH(false),
    ERROR_MIN_RANGE(true),
    ERROR_MAX_RANGE(true),
    ERROR_NO_MISSILE(true),
    ERROR_INVALID_MISSILE(true);

    public final boolean isError;

    LaunchStatus(boolean isError)
    {
        this.isError = isError;
    }
}
