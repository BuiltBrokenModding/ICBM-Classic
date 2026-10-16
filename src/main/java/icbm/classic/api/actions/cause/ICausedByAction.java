package icbm.classic.api.actions.cause;

import icbm.classic.api.actions.IActionData;

/**
 * Cause containing action information
 */
public interface ICausedByAction extends IActionCause {
    IActionSource getSource();

    IActionData getActionData();
}
// PORTED
