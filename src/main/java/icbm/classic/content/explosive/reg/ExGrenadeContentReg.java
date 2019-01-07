package icbm.classic.content.explosive.reg;

import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.reg.content.IExGrenadeRegistry;
import net.minecraft.util.ResourceLocation;

/**
 *
 * Created by Dark(DarkGuardsman, Robert) on 1/7/19.
 */
public class ExGrenadeContentReg extends ExplosiveContentRegistry implements IExGrenadeRegistry
{
    public ExGrenadeContentReg()
    {
        super(ICBMClassicAPI.EX_GRENADE);
    }
}
