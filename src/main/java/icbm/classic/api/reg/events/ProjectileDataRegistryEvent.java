package icbm.classic.api.reg.events;

import icbm.classic.api.missiles.projectile.IProjectileDataRegistry;
import net.neoforged.bus.api.Event;

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
// PORTED
