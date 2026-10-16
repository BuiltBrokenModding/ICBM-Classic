package icbm.classic.lib.actions.status;

import icbm.classic.ICBMConstants;
import icbm.classic.api.actions.status.ActionStatusTypes;
import icbm.classic.api.actions.status.IActionStatus;
import net.minecraft.world.entity.Entity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;

public class ActionResponses {
    // <editor-fold description="error status">
    /** Unexpected errors that don't align with any specific type... usually lazy dev using try-catch */
    public static IActionStatus UNKNOWN_ERROR = ImmutableStatus.error(ResourceLocation.fromNamespaceAndPath(ICBMConstants.DOMAIN, "action.error.unknown"));

    /** Spawning via {@link Level#addFreshEntity(Entity)} failed. Could be due to chunk unloaded, join world event, or other unknown reason */
    public static IActionStatus ENTITY_SPAWN_FAILED = ImmutableStatus.error(ResourceLocation.fromNamespaceAndPath(ICBMConstants.DOMAIN, "action.entity.spawning.failed"));

    /** Canceled due to {@link net.neoforged.neoforge.event.EventHooks#onExplosionStart(Level, Explosion)} */
    public static IActionStatus EXPLOSION_CANCELED = ImmutableStatus.blocking(ResourceLocation.fromNamespaceAndPath(ICBMConstants.DOMAIN, "action.explosion.canceled"));

    /** Generic validation issue with action state */
    public static IActionStatus VALIDATION_ERROR = ImmutableStatus.blocking(ResourceLocation.fromNamespaceAndPath(ICBMConstants.DOMAIN, "action.validation.failed"));

    public static IActionStatus BLOCK_PLACEMENT_FAILED = ImmutableStatus.blocking(ResourceLocation.fromNamespaceAndPath(ICBMConstants.DOMAIN, "action.block.placement.failed"));

    public static IActionStatus MISSING_WORLD = new MissingFieldStatus(ResourceLocation.fromNamespaceAndPath(ICBMConstants.DOMAIN, "action.error.field.world.missing"), "action", "world");
    public static IActionStatus MISSING_HOST = new MissingFieldStatus(ResourceLocation.fromNamespaceAndPath(ICBMConstants.DOMAIN, "action.error.field.host.missing"), "action", "host");
    public static IActionStatus MISSING_BLOCK_POS = new MissingFieldStatus(ResourceLocation.fromNamespaceAndPath(ICBMConstants.DOMAIN, "action.error.field.block.pos.missing"), "action", "blockPos");
    public static IActionStatus MISSING_BLOCK_STATE = new MissingFieldStatus(ResourceLocation.fromNamespaceAndPath(ICBMConstants.DOMAIN, "action.error.field.block.state.missing"), "action", "blockState");
    public static IActionStatus MISSING_VALIDATION = new MissingFieldStatus(ResourceLocation.fromNamespaceAndPath(ICBMConstants.DOMAIN, "action.error.field.validation.missing"), "action", "validation");

    public static IActionStatus MISSING_ACTION_DATA = new MissingFieldStatus(ResourceLocation.fromNamespaceAndPath(ICBMConstants.DOMAIN, "action.error.field.action.data"), "runner", "actionData");
    // </editor-fold>

    // <editor-fold description="good status">
    public static IActionStatus COMPLETED = ImmutableStatus.create(ResourceLocation.fromNamespaceAndPath(ICBMConstants.DOMAIN, "action.completed"), ActionStatusTypes.DONE);
    public static IActionStatus READY = ImmutableStatus.create(ResourceLocation.fromNamespaceAndPath(ICBMConstants.DOMAIN, "action.ready"), ActionStatusTypes.READY);
    public static IActionStatus WORKING = ImmutableStatus.create(ResourceLocation.fromNamespaceAndPath(ICBMConstants.DOMAIN, "action.working"), ActionStatusTypes.BLOCKING, ActionStatusTypes.WAITING);
    public static IActionStatus WAITING = ImmutableStatus.create(ResourceLocation.fromNamespaceAndPath(ICBMConstants.DOMAIN, "action.waiting"), ActionStatusTypes.BLOCKING, ActionStatusTypes.WAITING);
    // </editor-fold>

    public static void registerTypes() {

        UNKNOWN_ERROR.register();
        ENTITY_SPAWN_FAILED.register();
        EXPLOSION_CANCELED.register();
        VALIDATION_ERROR.register();
        BLOCK_PLACEMENT_FAILED.register();
        MISSING_WORLD.register();
        MISSING_HOST.register();
        MISSING_BLOCK_POS.register();
        MISSING_BLOCK_STATE.register();
        MISSING_VALIDATION.register();
        COMPLETED.register();
        READY.register();
        WORKING.register();
        WAITING.register();
    }
}
// PORTED
