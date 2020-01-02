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
 * Fired to allow registering new explosive types
 * <p>
 * Created by Dark(DarkGuardsman, Robert) on 1/4/19.
 */
public class ExplosiveRegistryEvent extends Event
{

    public final IExplosiveRegistry registry;

    private ResourceLocation lastRegistered;//TODO remove  after moving enable to a separate event

    public ExplosiveRegistryEvent(IExplosiveRegistry registry)
    {
        this.registry = registry;
    }

    /**
     * Called to register a new explosive
     *
     * @param id           - unique id
     * @param tier         - tier of the explosive, optional but should default to something
     * @param blastFactory - handler to build the blast from the explosive type
     * @return this event so we can chain calls //TODO remove return after moving enable to a separate event
     */
    public ExplosiveRegistryEvent register(ResourceLocation id, EnumTier tier, IBlastFactory blastFactory)
    {
        ICBMClassicAPI.EXPLOSIVE_REGISTRY.register(id, tier, blastFactory);
        lastRegistered = id;
        return this;
    }

    //TODO move to a separate event
    public ExplosiveRegistryEvent enableContent(ResourceLocation contentID)
    {
        return enableContent(lastRegistered, contentID);
    }

    //TODO move to a separate event
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

    //TODO remove after moving enable to a separate event
    public void done()
    {
        lastRegistered = null;
    }
}
