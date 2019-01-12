package icbm.classic.lib.explosive.reg;

import com.google.common.collect.ImmutableSet;
import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.reg.content.IExplosiveContentRegistry;
import icbm.classic.api.reg.IExplosiveData;
import net.minecraft.util.ResourceLocation;

import java.util.HashSet;
import java.util.Set;

/**
 *
 * Created by Dark(DarkGuardsman, Robert) on 1/4/19.
 */
public abstract class ExplosiveContentRegistry implements IExplosiveContentRegistry
{

    public final ResourceLocation name;

    //set of IDs enabled for content
    private Set<Integer> enabledIDs = new HashSet();

    private boolean locked = false;

    private Set<ResourceLocation> nameCache;
    private Set<IExplosiveData> dataCache;

    public ExplosiveContentRegistry(ResourceLocation name)
    {
        this.name = name;
    }

    @Override
    public ResourceLocation getRegistryName()
    {
        return name;
    }

    @Override
    public Set<Integer> getExplosivesIDs()
    {
        return enabledIDs;
    }

    @Override
    public void enableContent(ResourceLocation explosiveID)
    {
        if (locked)
        {
            throw new RuntimeException(this + ": No content can be registered after registry phase");
        }
        final IExplosiveData data = ICBMClassicAPI.EXPLOSIVE_REGISTRY.getExplosiveData(explosiveID);
        if (data != null)
        {
            //Add to enable list on object itself
            if (!data.onEnableContent(name, this))
            {
                //Explosive rejected the handler
                return;
            }

            //Add ID to set of ids
            enabledIDs.add(data.getRegistryID());
        }
    }

    @Override
    public Set<ResourceLocation> getExplosiveNames()
    {
        return nameCache;
    }

    @Override
    public Set<IExplosiveData> getExplosives()
    {
        return dataCache;
    }

    @Override
    public void lockRegistry()
    {
        locked = true;
        enabledIDs = ImmutableSet.copyOf(enabledIDs);

        dataCache = enabledIDs.stream()
                .map(id -> ICBMClassicAPI.EXPLOSIVE_REGISTRY.getExplosiveData(id))
                .filter(e -> e != null)
                .collect(ImmutableSet.toImmutableSet());

        nameCache = dataCache.stream().map(data -> data.getRegistryName()).collect(ImmutableSet.toImmutableSet());
    }

    @Override
    public String toString()
    {
        return "ExplosiveContentRegistry[" + name + "]";
    }
}
