package icbm.classic.api.reg.content;

import icbm.classic.api.data.BlockActivateFunction;
import net.minecraft.resources.ResourceLocation;

/**
 * @deprecated being removed in 1.13
 */
@Deprecated
public interface IExBlockRegistry extends IExplosiveContentRegistry, IExFuseBlockRegistry
{
    /**
     * Called to set a function to invoke when an explosive block is clicked
     * <p>
     * Do not use this in place of normal events, this is designed to add logic for
     * specific block types.
     *
     * @param exName
     * @param function
     */
    void setActivationListener(ResourceLocation exName, BlockActivateFunction function);
}
// PORTED
