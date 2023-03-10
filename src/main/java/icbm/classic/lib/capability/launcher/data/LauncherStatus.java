package icbm.classic.lib.capability.launcher.data;

import icbm.classic.ICBMConstants;
import icbm.classic.api.launcher.IActionStatus;
import icbm.classic.content.blocks.launcher.LauncherLangs;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;


public class LauncherStatus implements IActionStatus {

    public static final LauncherStatus ERROR_GENERIC = new LauncherStatus(true, LauncherLangs.ERROR);
    public static final LauncherStatus ERROR_SPAWN = new LauncherStatus(true, LauncherLangs.ERROR_MISSILE_SPAWNING);
    public static final LauncherStatus ERROR_MIN_RANGE = new LauncherStatus(true, LauncherLangs.ERROR_TARGET_MIN); //TODO use factory to provide range
    public static final LauncherStatus ERROR_MAX_RANGE = new LauncherStatus(true, LauncherLangs.ERROR_TARGET_MAX);
    public static final LauncherStatus ERROR_TARGET_NULL = new LauncherStatus(true, LauncherLangs.ERROR_TARGET_NONE);
    public static final LauncherStatus ERROR_POWER = new LauncherStatus(true, LauncherLangs.ERROR_NO_POWER);
    public static final LauncherStatus ERROR_INVALID_STACK = new LauncherStatus(true, LauncherLangs.ERROR_MISSILE_INVALID);
    public static final LauncherStatus ERROR_EMPTY_STACK = new LauncherStatus(true, LauncherLangs.ERROR_MISSILE_NONE);

    public static final LauncherStatus LAUNCHED = new LauncherStatus(false, LauncherLangs.STATUS_LAUNCHED);
    public static final LauncherStatus CANCELED = new LauncherStatus(false, LauncherLangs.STATUS_CANCELED);

    private final boolean error;
    private final ITextComponent message;

    public LauncherStatus(boolean isError, String translationKey) {
        this.error = isError;
        this.message = new TextComponentTranslation(translationKey);
    }

    @Override
    public boolean isError() {
        return error;
    }

    @Override
    public ITextComponent message() {
        return message;
    }
}
