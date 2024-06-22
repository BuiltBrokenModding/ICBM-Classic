package icbm.classic.content.blocks.launcher.status;

import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.lib.actions.status.ActionStatus;
import icbm.classic.content.blocks.launcher.LauncherLangs;
import lombok.NoArgsConstructor;


@NoArgsConstructor
public final class LauncherStatus {

    // Errors
    public static final ActionStatus ERROR_GENERIC = new ActionStatus().withRegName("launcher.error.generic").asError().withTranslation(LauncherLangs.ERROR);
    public static final ActionStatus ERROR_SPAWN = new ActionStatus().withRegName("launcher.error.spawning").asError().withTranslation(LauncherLangs.ERROR_MISSILE_SPAWNING);
    public static final ActionStatus ERROR_SPAWN_COLLIDER = new ActionStatus().withRegName("launcher.error.spawning.space").asError().withTranslation(LauncherLangs.ERROR_MISSILE_SPACE);
    public static final ActionStatus ERROR_MIN_RANGE = new ActionStatus().withRegName("launcher.error.range.min").asError().withTranslation(LauncherLangs.ERROR_TARGET_MIN); //TODO use factory to provide range
    public static final ActionStatus ERROR_MAX_RANGE = new ActionStatus().withRegName("launcher.error.range.max").asError().withTranslation(LauncherLangs.ERROR_TARGET_MAX);
    public static final ActionStatus ERROR_TARGET_NULL = new ActionStatus().withRegName("launcher.error.target.null").asError().withTranslation(LauncherLangs.ERROR_TARGET_NONE);
    public static final ActionStatus ERROR_POWER = new ActionStatus().withRegName("launcher.error.power").asError().withTranslation(LauncherLangs.ERROR_NO_POWER);
    public static final ActionStatus ERROR_INVALID_STACK = new ActionStatus().withRegName("launcher.error.missile.invalid").asError().withTranslation(LauncherLangs.ERROR_MISSILE_INVALID);
    public static final ActionStatus ERROR_EMPTY_STACK = new ActionStatus().withRegName("launcher.error.missile.empty").asError().withTranslation(LauncherLangs.ERROR_MISSILE_NONE);
    public static final ActionStatus ERROR_QUEUED = new ActionStatus().withRegName("launcher.error.missile.queued").asError().withTranslation(LauncherLangs.ERROR_MISSILE_QUEUED);
    public static final ActionStatus ERROR_EMPTY_GROUP = new ActionStatus().withRegName("launcher.error.group.empty").asError().withTranslation(LauncherLangs.ERROR_GROUP_EMPTY);
    public static final ActionStatus ERROR_NO_NETWORK = new ActionStatus().withRegName("launcher.error.network.none").asError().withTranslation(LauncherLangs.ERROR_NO_NETWORK);

    // Responses
    public static final ActionStatus READY = new ActionStatus().withRegName("launcher.ready").withTranslation(LauncherLangs.STATUS_READY);
    public static final ActionStatus LAUNCHED = new ActionStatus().withRegName("launcher.launched").withTranslation(LauncherLangs.STATUS_LAUNCHED);
    public static final ActionStatus CANCELED = new ActionStatus().withRegName("launcher.canceled").withTranslation(LauncherLangs.STATUS_CANCELED);

    // Active states
    public static final ActionStatus FIRING_AIMING = new ActionStatus().withRegName("launcher.firing.aiming").asBlocking().withTranslation(LauncherLangs.STATUS_FIRING_AIMING);

    public static void registerTypes() {

        register(ERROR_GENERIC);
        register(ERROR_SPAWN);
        register(ERROR_SPAWN_COLLIDER);
        register(ERROR_MIN_RANGE);
        register(ERROR_MAX_RANGE);
        register(ERROR_TARGET_NULL);
        register(ERROR_POWER);
        register(ERROR_INVALID_STACK);
        register(ERROR_EMPTY_STACK);
        register(ERROR_QUEUED);
        register(LAUNCHED);
        register(READY);
        register(CANCELED);
        register(FIRING_AIMING);

        ICBMClassicAPI.ACTION_STATUS_REGISTRY.register(FiringWithDelay.REG_NAME, FiringWithDelay::new);
        ICBMClassicAPI.ACTION_STATUS_REGISTRY.register(LaunchedWithMissile.REG_NAME, LaunchedWithMissile::new);
    }

    private static void register(ActionStatus constantStatus) {
        ICBMClassicAPI.ACTION_STATUS_REGISTRY.register(constantStatus.getRegistryKey(), () -> constantStatus);
    }
}
