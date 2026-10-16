package icbm.classic.api.reg.events;

import icbm.classic.api.actions.IActionData;
import icbm.classic.api.reg.obj.IBuilderRegistry;
import net.neoforged.bus.api.Event;

/**
 * Fired to allow registering action data message types
 */
public class ActionRegistryEvent extends Event
{
    public final IBuilderRegistry<IActionData> registry;

    public ActionRegistryEvent(IBuilderRegistry<IActionData> registry)
    {
        this.registry = registry;
    }
}
// PORTED
