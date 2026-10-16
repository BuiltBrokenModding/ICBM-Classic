package icbm.classic.api.actions;

import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.actions.cause.IActionCause;
import icbm.classic.api.actions.status.IActionStatus;
import icbm.classic.api.reg.obj.IBuildableObject;
import icbm.classic.api.reg.obj.IBuilderRegistry;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A potential action to triggered once conditions are met. Included
 * everything that is needed to validate and trigger an action. Short of
 * the location and caused by source.
 *
 * Potential actions are designed to be cached on source. Such as having a field
 * at the top of a BlockEntity called warheadAction.
 */
public interface IPotentialAction extends IBuildableObject {

    /**
     * Action data used to spawn the action into the world
     *
     * @return action data, null if it failed to load or get by key
     */
    @Nullable
    IActionData getActionData();

    /**
     * Checks the current status of the solution. Validating if requirements are met and nothing
     * is going to fail when action is triggered is called.
     *
     * This is normally called as part of action trigger and
     * only exists as a way to show predicted result. Useful for user feedback related to potential actions.
     *
     * @param world to create action
     * @param x location
     * @param y location
     * @param z location
     * @param cause of the action, not recommended to leave null
     * @return response
     */
    @Nonnull
    IActionStatus checkAction(Level world, double x, double y, double z, @Nullable IActionCause cause) ;

    /**
     * Wraps to {@link IActionData} after validating
     * solution state and if conditions are met to trigger.
     *
     * @param world to create action
     * @param x location
     * @param y location
     * @param z location
     * @param cause of the action, not recommended to leave null
     * @return response
     */
    @Nonnull
    IActionStatus doAction(Level world, double x, double y, double z, @Nullable IActionCause cause);

    @Override
    default IBuilderRegistry getRegistry() {
        return ICBMClassicAPI.ACTION_POTENTIAL_REGISTRY;
    }
}
// PORTED
