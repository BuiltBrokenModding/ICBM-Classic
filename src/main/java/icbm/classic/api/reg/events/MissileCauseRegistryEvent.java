package icbm.classic.api.reg.events;

import icbm.classic.api.missiles.cause.IMissileCause;
import icbm.classic.api.reg.obj.IMissilePartReg;
import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * Fired to allow registering missile source builders
 */
public class MissileCauseRegistryEvent extends Event
{
    public final IMissilePartReg<IMissileCause> registry;

    public MissileCauseRegistryEvent(IMissilePartReg<IMissileCause> registry)
    {
        this.registry = registry;
    }
}
