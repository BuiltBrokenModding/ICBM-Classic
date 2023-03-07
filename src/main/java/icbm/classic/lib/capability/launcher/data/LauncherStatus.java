package icbm.classic.lib.capability.launcher.data;

import icbm.classic.ICBMConstants;
import icbm.classic.api.launcher.IActionStatus;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class LauncherStatus implements IActionStatus {

    private static final String ERROR_PREFIX = "launcher.error." + ICBMConstants.PREFIX + "message";
    public static final LauncherStatus ERROR_GENERIC = new LauncherStatus(true, ERROR_PREFIX + ".generic");
    public static final LauncherStatus ERROR_SPAWN = new LauncherStatus(true, ERROR_PREFIX + ".spawning");
    public static final LauncherStatus ERROR_MIN_RANGE = new LauncherStatus(true, ERROR_PREFIX + ".range.min"); //TODO use factory to provide range
    public static final LauncherStatus ERROR_MAX_RANGE = new LauncherStatus(true, ERROR_PREFIX + ".range.min");
    public static final LauncherStatus ERROR_TARGET_NULL = new LauncherStatus(true, ERROR_PREFIX + ".target.null");
    public static final LauncherStatus ERROR_POWER = new LauncherStatus(true, ERROR_PREFIX + ".power");
    public static final LauncherStatus ERROR_INVALID_STACK = new LauncherStatus(true, ERROR_PREFIX + ".slot.missile.invalid");
    public static final LauncherStatus ERROR_EMPTY_STACK = new LauncherStatus(true, ERROR_PREFIX + ".slot.missile.empty");

    private static final String STATUS_PREFIX = "launcher.status." + ICBMConstants.PREFIX + "message";
    public static final LauncherStatus LAUNCHED = new LauncherStatus(false, STATUS_PREFIX + ".launched");
    public static final LauncherStatus CANCELED = new LauncherStatus(false, STATUS_PREFIX + ".canceled");

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
