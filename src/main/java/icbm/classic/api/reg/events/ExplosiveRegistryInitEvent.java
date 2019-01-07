package icbm.classic.api.reg.events;

import icbm.classic.api.reg.content.IExplosiveContentRegistry;
import icbm.classic.api.reg.IExplosiveRegistry;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.eventhandler.Event;

/**
 *
 * Created by Dark(DarkGuardsman, Robert) on 1/4/19.
 */
public class ExplosiveRegistryInitEvent extends Event
{
    public final IExplosiveRegistry registry;

    public ExplosiveRegistryInitEvent(IExplosiveRegistry registry)
    {
        this.registry = registry;
    }

    public void register(ResourceLocation id, IExplosiveContentRegistry contentRegistry)
    {
        registry.registerContentRegistry(id, contentRegistry);
    }
}
