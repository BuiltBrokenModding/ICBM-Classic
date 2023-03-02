package icbm.classic.api.launcher;

/**
 * Status response for firing a missile
 */
public interface IMissileLauncherStatus {

    /**
     * Is the status an error state
     *
     * @return true if error
     */
    boolean isError();

    /**
     * Localization for error output. Assume this
     * will be displayed to a user.
     *
     * @return message
     */
    String message();
}
