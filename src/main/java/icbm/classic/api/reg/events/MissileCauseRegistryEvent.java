package icbm.classic.api.reg.events;

import icbm.classic.api.missiles.cause.IMissileCause;
import icbm.classic.api.reg.obj.IBuilderRegistry;
import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * Fired to allow registering missile source builders
 */
public class MissileCauseRegistryEvent extends Event
{
    public final IBuilderRegistry<IMissileCause> registry;

    public MissileCauseRegistryEvent(IBuilderRegistry<IMissileCause> registry)
    {
        this.registry = registry;
    }
}
