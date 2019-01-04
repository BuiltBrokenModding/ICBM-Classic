package icbm.classic.api.reg;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
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
