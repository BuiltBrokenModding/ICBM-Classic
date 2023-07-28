package icbm.classic.api.reg.events;

import icbm.classic.api.missiles.projectile.IProjectileDataRegistry;
import icbm.classic.api.reg.IExplosiveCustomization;
import icbm.classic.api.reg.obj.IBuilderRegistry;
import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * Fired to allow registering projectile data
 */
public class ProjectileDataRegistryEvent extends Event
{
    public final IProjectileDataRegistry registry;

    public ProjectileDataRegistryEvent(IProjectileDataRegistry registry)
    {
        this.registry = registry;
    }
}
