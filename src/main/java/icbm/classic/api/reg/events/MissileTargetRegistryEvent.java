package icbm.classic.api.reg.events;

import icbm.classic.api.missiles.parts.IMissileTarget;
import icbm.classic.api.reg.obj.IBuilderRegistry;
import net.neoforged.bus.api.Event;

/**
 * Fired to allow registering missile target builders
 */
public class MissileTargetRegistryEvent extends Event {
    public final IBuilderRegistry<IMissileTarget> registry;

    public MissileTargetRegistryEvent(IBuilderRegistry<IMissileTarget> registry) {
        this.registry = registry;
    }
}
