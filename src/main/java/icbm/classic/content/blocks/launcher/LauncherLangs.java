package icbm.classic.content.blocks.launcher;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;

@NoArgsConstructor(access = AccessLevel.NONE)
public final class LauncherLangs {

    public static final String ERROR = "info.icbmclassic:launcher.error";
    public static final String ERROR_NO_POWER = ERROR + ".power";
    public static final String ERROR_NO_FUEL = ERROR + ".fuel";
    public static final String ERROR_NO_NETWORK = ERROR + ".network";
    public static final String ERROR_NO_LAUNCHER = ERROR + ".missing";

    public static final String ERROR_MISSILE_NONE = ERROR + ".missile.none";
    public static final String ERROR_MISSILE_INVALID = ERROR + ".missile.invalid";
    public static final String ERROR_MISSILE_SPACE = ERROR + ".missile.space";
    public static final String ERROR_MISSILE_SPAWNING = ERROR + ".missile.spawn";

    public static final String ERROR_TARGET_NONE = ERROR + ".target.none";
    public static final String ERROR_TARGET_MIN = ERROR + ".target.min";
    public static final String ERROR_TARGET_MAX = ERROR + ".target.max";
    public static final String ERROR_TARGET_ANGLE = ERROR + ".target.angle";

    public static final String STATUS = "info.icbmclassic:launcher.status";
    public static final String STATUS_READY = STATUS + ".ready";
    public static final String STATUS_LAUNCHED = STATUS + ".launched";
    public static final String STATUS_CANCELED = STATUS + ".canceled";

    // TODO consider caching translation components to save on memory

    public static final ITextComponent TRANSLATION_READY = new TextComponentTranslation(LauncherLangs.STATUS_READY);
    public static final ITextComponent TRANSLATION_ERROR_NO_NETWORK = new TextComponentTranslation(LauncherLangs.ERROR_NO_NETWORK);

    public static final ITextComponent TRANSLATION_TOOLTIP_RADIO = new TextComponentTranslation("gui.icbmclassic:tooltip.radio.channel");
    public static final ITextComponent TRANSLATION_TOOLTIP_TARGET = new TextComponentTranslation("gui.icbmclassic:tooltip.target");
}
