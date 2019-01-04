package icbm.classic.content.explosive.reg;

import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.reg.content.IExplosiveContentRegistry;
import icbm.classic.api.reg.IExplosiveData;
import net.minecraft.util.ResourceLocation;

import java.util.HashSet;
import java.util.Set;

/**
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 1/4/19.
 */
public class ExplosiveContentRegistry implements IExplosiveContentRegistry
{
    public final ResourceLocation name;

    //set of IDs enabled for content
    public final Set<Integer> enabledIDs = new HashSet();

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
}
