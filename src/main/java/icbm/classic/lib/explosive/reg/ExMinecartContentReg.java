package icbm.classic.lib.explosive.reg;

import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.reg.IExplosiveData;
import icbm.classic.api.reg.content.IExMinecartRegistry;
import icbm.classic.content.reg.ItemReg;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

/**
 *
 * Created by Dark(DarkGuardsman, Robert) on 1/7/19.
 */
public class ExMinecartContentReg extends ExplosiveContentRegistry implements IExMinecartRegistry
{
    public ExMinecartContentReg()
    {
        super(ICBMClassicAPI.EX_MINECART);
    }

    @Override
    public ItemStack getDeviceStack(ResourceLocation regName)
    {
        IExplosiveData ex = getExplosive(regName);
        if(ex != null)
        {
            return new ItemStack(ItemReg.itemBombCart, 1, ex.getRegistryID());
        }
        return null;
    }
}
