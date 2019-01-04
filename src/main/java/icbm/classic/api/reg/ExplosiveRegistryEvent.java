package icbm.classic.api.reg;

import icbm.classic.api.explosion.IBlastFactory;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 1/4/19.
 */
public class ExplosiveRegistryEvent extends Event
{
    public final IExplosiveRegistry registry;

    public ExplosiveRegistryEvent(IExplosiveRegistry registry)
    {
        this.registry = registry;
    }

    public ExplosiveRegistryEvent register(ResourceLocation id, IBlastFactory blastFactory)
    {
        return this;
    }
}
