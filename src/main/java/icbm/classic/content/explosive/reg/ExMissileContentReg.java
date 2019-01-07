package icbm.classic.content.explosive.reg;

import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.reg.content.IExMissileRegistry;
import net.minecraft.util.ResourceLocation;

/**
 *
 * Created by Dark(DarkGuardsman, Robert) on 1/7/19.
 */
public class ExMissileContentReg extends ExplosiveContentRegistry implements IExMissileRegistry
{
    public ExMissileContentReg()
    {
        super(ICBMClassicAPI.EX_MISSILE);
    }
}
