package icbm.classic.api.actions.conditions;

import icbm.classic.api.actions.cause.IActionCause;

/**
 * Special conditional that acts as a cause-by reason.
 *
 * Most systems will only consider end-cap conditions as cause-by. What this means is that if a conditional set was
 * (A & B & (C & D) & E) then only E  would be the end cap. Though it is possible to have several end caps for
 * consideration. Such as (A & B & (C || D || E)); Where C, D, E all can be an end cap. With the first to return
 * yes winning the cause-by.
 *
 * Reason for this is conditionals before the end are thought of as validations. Checked required in order to
 * validate the end-caps can trigger the outcome.
 *
 * A more specific example would be missile with conditional (DISTANCE_TRAVELED & (NEAR_TARGET || TIMER || IMPACT)).
 * Where DISTANCE_TRAVELED is a safety check. Then any three of the conditions after could trigger the warhead.
 */
public interface IConditionCause extends ICondition {
    IActionCause getCause();
}
// PORTED
