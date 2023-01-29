package icbm.classic.api.reg.events;

import icbm.classic.api.reg.obj.IMissileSourceReg;
import icbm.classic.api.reg.obj.IMissileTargetReg;
import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * Fired to allow registering missile source builders
 */
public class MissileSourceRegistryEvent extends Event
{
    public final IMissileSourceReg registry;

    public MissileSourceRegistryEvent(IMissileSourceReg registry)
    {
        this.registry = registry;
    }
}
