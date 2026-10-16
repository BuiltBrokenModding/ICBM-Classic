package icbm.classic.content.blocks.launcher;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.minecraft.network.chat.Component;

@NoArgsConstructor(access = AccessLevel.NONE)
public final class LauncherLangs {

    public static final String ERROR = "info.icbmclassic:launcher.error";
    public static final String ERROR_NO_POWER = ERROR + ".power";
    public static final String ERROR_NO_FUEL = ERROR + ".fuel";

    public static final String ERROR_NO_NETWORK = ERROR + ".network";
    public static final String ERROR_NO_NETWORK_STATUS = ERROR + ".network.status.missing";
    public static final String ERROR_NO_LAUNCHER = ERROR + ".network.launchers"; // PORT: 1.12 was missing the '.', so the lang key never matched

    public static final String ERROR_MISSILE_NONE = ERROR + ".missile.none";
    public static final String ERROR_MISSILE_INVALID = ERROR + ".missile.invalid";
    public static final String ERROR_MISSILE_SPACE = ERROR + ".missile.space";
    public static final String ERROR_MISSILE_SPAWNING = ERROR + ".missile.spawn";
    public static final String ERROR_MISSILE_QUEUED = ERROR + ".missile.queued";
    public static final String ERROR_MISSILE_MULTI = ERROR + ".missile.multi";
    public static final String ERROR_GROUP_EMPTY = ERROR + ".group.empty";

    public static final String ERROR_TARGET_NONE = ERROR + ".target.none";
    public static final String ERROR_TARGET_MIN = ERROR + ".target.min";
    public static final String ERROR_TARGET_MAX = ERROR + ".target.max";
    public static final String ERROR_TARGET_ANGLE = ERROR + ".target.angle";

    public static final String STATUS = "info.icbmclassic:launcher.status";
    public static final String STATUS_READY = STATUS + ".ready";
    public static final String STATUS_LAUNCHED = STATUS + ".launched";
    public static final String STATUS_CANCELED = STATUS + ".canceled";

    public static final String STATUS_FIRING = STATUS + ".firing";
    public static final String STATUS_FIRING_AIMING = STATUS_FIRING + ".aiming";
    public static final String STATUS_FIRING_DELAYED = STATUS_FIRING + ".delayed";

    // TODO consider caching translation components to save on memory

    public static final Component TRANSLATION_READY = Component.translatable(LauncherLangs.STATUS_READY);
    public static final Component TRANSLATION_ERROR_NO_NETWORK = Component.translatable(LauncherLangs.ERROR_NO_NETWORK);
    public static final Component TRANSLATION_ERROR_NO_LAUNCHER = Component.translatable(LauncherLangs.ERROR_NO_LAUNCHER);
    public static final Component TRANSLATION_ERROR_NO_NETWORK_STATUS = Component.translatable(LauncherLangs.ERROR_NO_NETWORK_STATUS);

    public static final Component TRANSLATION_TOOLTIP_RADIO = Component.translatable("gui.icbmclassic:tooltip.radio.channel");
    public static final Component TRANSLATION_TOOLTIP_TARGET = Component.translatable("gui.icbmclassic:tooltip.target");
}
// PORTED
