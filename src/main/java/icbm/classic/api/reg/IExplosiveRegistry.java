package icbm.classic.api.reg;

import icbm.classic.api.explosion.IBlastFactory;
import net.minecraft.util.ResourceLocation;

/**
 * Created by Dark(DarkGuardsman, Robin) on 1/4/19.
 *
 * @deprecated being replaced by {@link icbm.classic.api.ICBMClassicAPI#ACTION_REGISTRY}
 */
@Deprecated
public interface IExplosiveRegistry
{
    IExplosiveData register(ResourceLocation name, IBlastFactory blastFactory);

    IExplosiveData getExplosiveData(ResourceLocation name, boolean allowNull);
}
