package icbm.classic.content.explosive.reg;

import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.reg.content.IExBlockRegistry;
import net.minecraft.util.ResourceLocation;

/**
 *
 * Created by Dark(DarkGuardsman, Robert) on 1/7/19.
 */
public class ExBlockContentReg extends ExplosiveContentRegistry implements IExBlockRegistry
{
    public ExBlockContentReg()
    {
        super(ICBMClassicAPI.EX_BLOCK);
    }
}
