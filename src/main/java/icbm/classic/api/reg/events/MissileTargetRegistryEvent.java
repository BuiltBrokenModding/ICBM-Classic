package icbm.classic.api.reg.events;

import icbm.classic.api.missiles.parts.IMissileTarget;
import icbm.classic.api.reg.obj.IMissilePartReg;
import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * Fired to allow registering missile target builders
 */
public class MissileTargetRegistryEvent extends Event
{
    public final IMissilePartReg<IMissileTarget> registry;

    public MissileTargetRegistryEvent(IMissilePartReg<IMissileTarget> registry)
    {
        this.registry = registry;
    }
}
