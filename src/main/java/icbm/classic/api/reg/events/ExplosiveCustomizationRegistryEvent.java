package icbm.classic.api.reg.events;

import icbm.classic.api.reg.IExplosiveCustomization;
import icbm.classic.api.reg.obj.IBuilderRegistry;
import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * Fired to allow registering customization types
 */
public class ExplosiveCustomizationRegistryEvent extends Event
{
    public final IBuilderRegistry<IExplosiveCustomization> registry;

    public ExplosiveCustomizationRegistryEvent(IBuilderRegistry<IExplosiveCustomization> registry)
    {
        this.registry = registry;
    }
}
