package icbm.classic.lib.explosive.reg;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.reg.IExplosiveData;
import icbm.classic.api.reg.content.IExplosiveContentRegistry;
import net.minecraft.util.ResourceLocation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 *
 * Created by Dark(DarkGuardsman, Robert) on 1/4/19.
 */
public abstract class ExplosiveContentRegistry implements IExplosiveContentRegistry
{

    public final ResourceLocation name;



    private boolean locked = false;

    //set of IDs enabled for content
    private Set<Integer> idCache;
    private Set<ResourceLocation> nameCache = new HashSet();
    private Set<IExplosiveData> dataCache;

    //Quick ref map
    private Map<ResourceLocation, IExplosiveData> mapCache;

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
    public void enableContent(ResourceLocation regName)
    {
        if (locked)
        {
            throw new RuntimeException(this + ": No content can be registered after registry phase");
        }
        final IExplosiveData data = ICBMClassicAPI.EXPLOSIVE_REGISTRY.getExplosiveData(regName);
        if (data != null)
        {
            //Add to enable list on object itself
            if (!data.onEnableContent(name, this))
            {
                //Explosive rejected the handler
                return;
            }

            //Add ID to set of ids
            nameCache.add(regName);
        }
    }

    @Override
    public Set<Integer> getExplosivesIDs()
    {
        return idCache;
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
    public IExplosiveData getExplosive(ResourceLocation regName)
    {
        if(mapCache != null)
        {
            return mapCache.get(regName);
        }
        return null;
    }

    @Override
    public void lockRegistry()
    {
        if(!locked)
        {
            locked = true;

            //Turn into immutable
            nameCache = ImmutableSet.copyOf(nameCache);

            //Generate reference map
            final HashMap<ResourceLocation, IExplosiveData> map = new HashMap();
            for(ResourceLocation name : nameCache)
            {
                final IExplosiveData data = ICBMClassicAPI.EXPLOSIVE_REGISTRY.getExplosiveData(name);
                if(data != null)
                {
                    map.put(name, data);
                }
                else
                {
                    throw new RuntimeException(this + ": Failed to locate explosive by name " + name);
                }
            }
            mapCache = ImmutableMap.copyOf(map);

            //Map ids to cache
            idCache = map.values().stream().map(entry -> entry.getRegistryID()).collect(ImmutableSet.toImmutableSet());
            dataCache = map.values().stream().collect(ImmutableSet.toImmutableSet());
        }
        else
            throw new RuntimeException(this + ": Registry was locked twice!");
    }

    @Override
    public String toString()
    {
        return "ExplosiveContentRegistry[" + name + "]";
    }

    public boolean isLocked()
    {
        return locked;
    }
}
