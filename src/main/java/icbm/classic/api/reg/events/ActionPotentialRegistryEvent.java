package icbm.classic.api.reg.events;

import icbm.classic.api.actions.IPotentialAction;
import icbm.classic.api.reg.obj.IBuilderRegistry;
import net.neoforged.bus.api.Event;

/**
 * Fired to allow registering potential action builders
 */
public class ActionPotentialRegistryEvent extends Event
{
    public final IBuilderRegistry<IPotentialAction> registry;

    public ActionPotentialRegistryEvent(IBuilderRegistry<IPotentialAction> registry)
    {
        this.registry = registry;
    }
}
// PORTED
