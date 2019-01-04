package icbm.classic.content.explosive.reg;

import icbm.classic.api.explosion.IBlastFactory;
import icbm.classic.api.reg.IExplosiveData;
import net.minecraft.util.ResourceLocation;

import java.util.HashSet;
import java.util.Set;

/**
 * Handles storing data about an explosive in the {@link ExplosiveRegistry}
 *
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 1/4/19.
 */
public class ExplosiveData implements IExplosiveData
{
    public final ResourceLocation regName;
    public final int id;

    public IBlastFactory blastCreationFactory;

    public final Set<ResourceLocation> enabledContent = new HashSet();

    public ExplosiveData(ResourceLocation regName, int id)
    {
        this.regName = regName;
        this.id = id;
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
}
