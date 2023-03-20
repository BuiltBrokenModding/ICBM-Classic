package icbm.classic.api.reg.events;

import icbm.classic.api.reg.obj.IMissileTargetReg;
import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * Fired to allow registering missile target builders
 */
public class MissileTargetRegistryEvent extends Event
{
    public final IMissileTargetReg registry;

    public MissileTargetRegistryEvent(IMissileTargetReg registry)
    {
        this.registry = registry;
    }
}
