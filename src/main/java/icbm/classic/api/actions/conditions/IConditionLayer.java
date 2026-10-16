package icbm.classic.api.actions.conditions;

import icbm.classic.api.actions.data.IActionFieldProvider;

import java.util.List;

/**
 * Composition of several conditions to be viewed as a single condition
 *
 * Often used to do AND/OR combined logic, do not assume logic as some
 * combinations may be exclusive-or or other strange behavior.
 *
 * Shouldn't be used to create things like inverters. For that use IActionCondition with
 */
public interface IConditionLayer extends ICondition {

    /**
     * List of conditions, may be immutable so don't edit
     *
     * @return triggers on this layer
     */
    List<ICondition> getConditions();

    @Override
    default void onTick() {
        getConditions().forEach(ICondition::onTick);
    }

    @Override
    default void init(IActionFieldProvider provider) {
        getConditions().forEach(c -> c.init(provider));
    }

    @Override
    default void reset() {
        getConditions().forEach(ICondition::reset);
    }
}
// PORTED
