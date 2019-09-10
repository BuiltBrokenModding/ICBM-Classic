package icbm.classic.api.reg.events;

import icbm.classic.api.reg.content.IExplosiveContentRegistry;
import icbm.classic.api.reg.IExplosiveRegistry;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * Used to register new content types before explosives are registered.
 * <p>
 * Created by Dark(DarkGuardsman, Robert) on 1/4/19.
 */
public class ExplosiveContentRegistryEvent extends Event
{

    public final IExplosiveRegistry registry;

    public ExplosiveContentRegistryEvent(IExplosiveRegistry registry)
    {
        this.registry = registry;
    }

    /**
     * Called to register a new content type
     *
     * @param id              - unique name of the content type
     * @param contentRegistry - content type
     */
    public void register(ResourceLocation id, IExplosiveContentRegistry contentRegistry)
    {
        registry.registerContentRegistry(id, contentRegistry);
    }
}
