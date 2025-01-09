package icbm.classic.api.caps;

import icbm.classic.api.actions.IPotentialAction;
import icbm.classic.api.reg.IExplosiveData;

import javax.annotation.Nonnull;

/**
 * Used in capabilities to provide an explosive for usage
 * Created by Dark(DarkGuardsman, Robin) on 1/7/19.
 *
 * @deprecated will be replaced by a capacitity providing {@link IPotentialAction}
 */
public interface IExplosive
{

    /**
     * Gets the explosive provided
     *
     * @return explosive data
     */
    @Nonnull
    IExplosiveData getExplosiveData();

    /**
     * Called when the explosive is defused
     *
     * @deprecated will be moved to it's own capability down the road
     */
    @Deprecated
    default void onDefuse() //TODO add args on who defused and how
    {

    }
}
