package icbm.classic.api.reg.events;

import icbm.classic.api.reg.obj.IMissileFlightLogicReg;
import icbm.classic.api.reg.obj.IMissileTargetReg;
import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * Fired to allow registering missile flight logic builders
 */
public class MissileFlightLogicRegistryEvent extends Event
{
    public final IMissileFlightLogicReg registry;

    public MissileFlightLogicRegistryEvent(IMissileFlightLogicReg registry)
    {
        this.registry = registry;
    }
}
