package icbm.classic.api.actions.conditions;

import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.actions.data.IActionFieldProvider;
import icbm.classic.api.actions.status.IActionStatus;
import icbm.classic.api.reg.obj.IBuildableObject;
import icbm.classic.api.reg.obj.IBuilderRegistry;

import javax.annotation.Nonnull;

/**
 * Generic system for applying conditionals for anything.
 */
public interface ICondition extends IBuildableObject {

    /**
     * Called to load any data from host system
     */
    default void init(IActionFieldProvider provider) {

    }

    @Nonnull
    default IBuilderRegistry<ICondition> getRegistry() {
        return ICBMClassicAPI.CONDITION_REGISTRY;
    }

    /**
     * Called to tick update the condition
     *
     * <br><br>
     *
     * In cases of {@link IConditionLayer} this will not be called every tick if condition is behaving using <a href="https://en.wikipedia.org/wiki/Short-circuit_evaluation">short-circuit evaluation</a>.
     *
     * <br><br>
     *
     * Example: AND(impact(block), timer(5))
     * <pre>
     *  tick(1) : impact gets ticked and isMet is false, timer is never ticked resulting in timer=0 and isMet false, combined value from AND is false
     *  tick(2) : impact gets ticked and isMet is true,  timer is then ticked resulting in timer=1 and isMet false, combined value from AND is false
     *  tick(6) : impact gets ticked and isMet is true,  timer is then ticked resulting in timer=5 and isMet true, combined value from AND is true
     * </pre>
     *
     * Example: OR(impact(block), timer(5))
     * <pre>
     *  tick(1) : impact gets ticked and isMet is false, timer is then ticked resulting in timer=1 and isMet false, combined value from OR is false
     *  tick(2) : impact gets ticked and isMet is true,  timer is never ticked resulting in timer=1 and isMet false, combined value from OR is true
     * </pre>
     *
     * Though this can not always be assumed. As {@link IConditionLayer} has the option to use any rules for ticking. As it uses composition pattern
     * to abstract away as a single condition. Even though it could contain layers of conditions with nesting and different behaivor.
     *
     * <br><br>
     *
     * Example of this is AND(A, AND(B, C, D, OR(E, F, G))) with a more realistic version being
     *
     * <pre>
     *      AND(
     *          TIMER(10, 'safety'), // Think of this like a spin fuse in grenade launchers, prevents killing shooter
     *          DISTANCE(20, 'distance from shooter safety'), // same idea as timer but only starts running after delay is counted
     *          OR(
     *              TIMER(1000, 'no hit kill safety'),  // triggers warhead to prevent it hitting unknown
     *              IMPACT(BLOCk), // normal impact
     *              IMPACT(ENTITY) // normal impact
     *            )
     *          )
     * </pre>
     * */
    default void onTick()
    {
    }

    /**
     * Checks if the requirements are met for the condition to be considered passable.
     *
     * This call must be read-only and not mutate each time it is called in the same logic block. This means
     * any random should be done in {@link #onTick()} or outside by the host of this condition.
     *
     * Do not assume result can be checked only once. As conditions may change between ticks or dependent variables
     * may modify validation. It is up to the condition to decide cached or continuous. This
     * behavior is abstracted to avoid assumings behavior. So make sure to call each time before triggering logic.
     *
     * @return status of the condition, return a condition with {@link icbm.classic.api.actions.status.ActionStatusTypes#GREEN} to be considered
     * as yes. Anything else will be viewed as no condition and displayed to downstream systems/users.
     */
    IActionStatus getCondition();

    /**
     * Called to reset the condition
     */
    default void reset() {

    }
}
// PORTED
