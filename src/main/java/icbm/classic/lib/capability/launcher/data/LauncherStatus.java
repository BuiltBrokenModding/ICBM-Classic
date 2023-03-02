package icbm.classic.lib.capability.launcher.data;

import icbm.classic.ICBMConstants;
import icbm.classic.api.launcher.IMissileLauncherStatus;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class LauncherStatus implements IMissileLauncherStatus {

    private static final String ERROR_PREFIX = "error." + ICBMConstants.PREFIX + "message";
    public static final LauncherStatus ERROR_GENERIC = new LauncherStatus(true, ERROR_PREFIX + ".generic");
    public static final LauncherStatus ERROR_MIN_RANGE = new LauncherStatus(true, ERROR_PREFIX + ".range.min"); //TODO use factory to provide range
    public static final LauncherStatus ERROR_MAX_RANGE = new LauncherStatus(true, ERROR_PREFIX + ".range.min");

    private final boolean error;
    private final String message;

    @Override
    public boolean isError() {
        return error;
    }

    @Override
    public String message() {
        return message;
    }
}
