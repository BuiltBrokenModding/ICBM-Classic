package icbm.classic.api.reg.events;

import icbm.classic.api.launcher.IActionStatus;
import icbm.classic.api.reg.obj.IBuilderRegistry;
import net.neoforged.bus.api.Event;

/**
 * Fired to allow registering action status message types
 */
public class ActionStatusRegistryEvent extends Event {
    public final IBuilderRegistry<IActionStatus> registry;

    public ActionStatusRegistryEvent(IBuilderRegistry<IActionStatus> registry) {
        this.registry = registry;
    }
}
