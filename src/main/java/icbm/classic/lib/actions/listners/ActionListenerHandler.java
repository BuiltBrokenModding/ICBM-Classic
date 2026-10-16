package icbm.classic.lib.actions.listners;

import icbm.classic.api.actions.IAction;
import icbm.classic.api.actions.IActionData;
import icbm.classic.api.actions.listener.IActionListener;
import icbm.classic.api.actions.listener.IActionListenerHandler;
import icbm.classic.api.actions.status.IActionStatus;
import icbm.classic.api.data.meta.MetaTag;
import lombok.NoArgsConstructor;

/**
 * Handles firing events for actions using {@link IActionData#getTypeTags()} as the event channels
 */
@NoArgsConstructor()
public class ActionListenerHandler implements IActionListenerHandler {

    private final ActionListenerLayer root = new ActionListenerLayer(null);

    @Override
    public void addListener(IActionListener listener, MetaTag... tags) {
        for (MetaTag tag : tags) {
            root.add(listener, tag);
        }
    }

    @Override
    public IActionStatus runAction(IAction action) {
        final IActionStatus status = root.onAction(action);
        if(status != null) {
            return status;
        }
        return action.doAction();
    }
}
// PORTED
