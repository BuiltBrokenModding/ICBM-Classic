package icbm.classic.content.explosive.reg;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.reg.content.IExplosiveContentRegistry;
import icbm.classic.api.reg.IExplosiveData;
import net.minecraft.util.ResourceLocation;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 1/4/19.
 */
public class ExplosiveContentRegistry implements IExplosiveContentRegistry
{

    public final ResourceLocation name;

    //set of IDs enabled for content
    private Set<Integer> enabledIDs = new HashSet();

    private boolean locked = true;

    private List<ResourceLocation> nameCache;
    private List<IExplosiveData> dataCache;

    public ExplosiveContentRegistry(ResourceLocation name)
    {
        this.name = name;
    }

    @Override
    public Set<Integer> getExplosivesEnabledForContent(ResourceLocation contentID)
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
    public List<ResourceLocation> getExplosiveNames()
    {
        return nameCache;
    }

    @Override
    public List<IExplosiveData> getExplosives()
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
                .collect(ImmutableList.toImmutableList());

        nameCache = dataCache.stream().map(data -> data.getRegistryName()).collect(ImmutableList.toImmutableList());
    }

    @Override
    public String toString()
    {
        return "ExplosiveContentRegistry[" + name + "]";
    }
}
