package icbm.classic.api.reg.events;

import icbm.classic.api.missiles.parts.IMissileFlightLogic;
import icbm.classic.api.reg.obj.IMissilePartReg;
import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * Fired to allow registering missile flight logic builders
 */
public class MissileFlightLogicRegistryEvent extends Event
{
    public final IMissilePartReg<IMissileFlightLogic> registry;

    public MissileFlightLogicRegistryEvent(IMissilePartReg<IMissileFlightLogic> registry)
    {
        this.registry = registry;
    }
}
