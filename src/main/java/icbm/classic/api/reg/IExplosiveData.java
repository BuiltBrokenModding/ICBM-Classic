package icbm.classic.api.reg;

import icbm.classic.api.explosion.IBlastFactory;
import net.minecraft.util.ResourceLocation;

/**
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 1/4/19.
 */
public interface IExplosiveData
{
    ResourceLocation getRegistryName();

    int getRegistryID();

    IBlastFactory getBlastFactory();
}
