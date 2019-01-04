package icbm.classic.api.reg.events;

import icbm.classic.ICBMClassic;
import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.EnumTier;
import icbm.classic.api.explosion.IBlastFactory;
import icbm.classic.api.reg.content.IExplosiveContentRegistry;
import icbm.classic.api.reg.IExplosiveRegistry;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 1/4/19.
 */
public class ExplosiveRegistryEvent extends Event
{
    public final IExplosiveRegistry registry;

    private ResourceLocation lastRegistered;

    public ExplosiveRegistryEvent(IExplosiveRegistry registry)
    {
        this.registry = registry;
    }

    public ExplosiveRegistryEvent register(ResourceLocation id, EnumTier tier, IBlastFactory blastFactory)
    {
        ICBMClassicAPI.EXPLOSIVE_REGISTRY.register(id,tier, blastFactory);
        lastRegistered = id;
        return this;
    }

    public ExplosiveRegistryEvent enableContent(ResourceLocation contentID)
    {
        return enableContent(lastRegistered, contentID);
    }

    public ExplosiveRegistryEvent enableContent(ResourceLocation id, ResourceLocation contentID)
    {
        IExplosiveContentRegistry registry = ICBMClassicAPI.EXPLOSIVE_REGISTRY.getContentRegistry(contentID);
        if (registry != null)
        {
            registry.enableContent(id);
        }
        else
        {
            ICBMClassic.logger().error("ExplosiveRegistryEvent: No content registry found for " + contentID + " while enabling content for " + id);
        }
        return this;
    }

    public void done()
    {
        lastRegistered = null;
    }
}
