package icbm.classic.lib.explosive.reg;

import icbm.classic.api.EnumTier;
import icbm.classic.api.explosion.IBlastFactory;
import icbm.classic.api.reg.content.IExplosiveContentRegistry;
import icbm.classic.api.reg.IExplosiveData;
import net.minecraft.util.ResourceLocation;

import java.util.HashSet;
import java.util.Set;

/**
 * Handles storing data about an explosive in the {@link ExplosiveRegistry}
 *
 *
 * Created by Dark(DarkGuardsman, Robert) on 1/4/19.
 */
public class ExplosiveData implements IExplosiveData
{
    public final ResourceLocation regName;
    public final int id;
    public final EnumTier tier;

    public IBlastFactory blastCreationFactory;

    public final Set<ResourceLocation> enabledContent = new HashSet();

    public boolean enabled = true;

    public ExplosiveData(ResourceLocation regName, int id, EnumTier tier)
    {
        this.regName = regName;
        this.id = id;
        this.tier = tier;
    }

    public ExplosiveData blastFactory(IBlastFactory factory)
    {
        blastCreationFactory = factory;
        return this;
    }

    @Override
    public ResourceLocation getRegistryName()
    {
        return regName;
    }

    @Override
    public int getRegistryID()
    {
        return id;
    }

    @Override
    public IBlastFactory getBlastFactory()
    {
        return blastCreationFactory;
    }

    @Override
    public EnumTier getTier()
    {
        return tier;
    }

    @Override
    public boolean isEnabled()
    {
        return enabled;
    }

    @Override
    public void setEnabled(boolean b)
    {
        this.enabled = b;
    }

    @Override
    public boolean onEnableContent(ResourceLocation contentID, IExplosiveContentRegistry registry)
    {
        enabledContent.add(contentID);
        return true;
    }

    @Override
    public boolean equals(Object object)
    {
        if(object instanceof ExplosiveData)
        {
            return ((ExplosiveData) object).id == id;
        }
        return false;
    }

    @Override
    public int compareTo(IExplosiveData o)
    {
        return Integer.compare(getRegistryID(), o.getRegistryID());
    }
}
