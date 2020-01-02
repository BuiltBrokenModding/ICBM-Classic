package icbm.classic.api.reg.content;

import icbm.classic.api.data.BlockActivateFunction;
import net.minecraft.util.ResourceLocation;

/**
 * Created by Dark(DarkGuardsman, Robert) on 1/4/19.
 */
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
