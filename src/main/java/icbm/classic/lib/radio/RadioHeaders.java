package icbm.classic.lib.radio;

/**
 * Enum of radio wave headers for messages
 *
 * Do not use external, these eventually will change
 */
public enum RadioHeaders {

    FIRE_AT_TARGET("activateLauncherWithTarget"),
    FIRE_LAUNCHER("activateLauncher"),
    SAM_TRIGGER("fireAntiMissile"); //TODO implement in launcher

    public final String header;

    RadioHeaders(String header) {
        this.header = header;
    }
}
