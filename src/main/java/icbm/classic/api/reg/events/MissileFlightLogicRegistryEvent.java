package icbm.classic.api.reg.events;

import icbm.classic.api.missiles.parts.IMissileFlightLogic;
import icbm.classic.api.reg.obj.IBuilderRegistry;
import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * Fired to allow registering missile flight logic builders
 */
public class MissileFlightLogicRegistryEvent extends Event
{
    public final IBuilderRegistry<IMissileFlightLogic> registry;

    public MissileFlightLogicRegistryEvent(IBuilderRegistry<IMissileFlightLogic> registry)
    {
        this.registry = registry;
    }
}
