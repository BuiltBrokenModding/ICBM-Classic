package icbm.classic.api;

import icbm.classic.ICBMClassic;
import icbm.classic.api.caps.IExplosive;
import icbm.classic.api.caps.IGPSData;
import icbm.classic.api.launcher.IMissileLauncher;
import icbm.classic.api.missiles.IMissile;
import icbm.classic.api.refs.ICBMExplosives;
import icbm.classic.api.reg.IExplosiveData;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;

@Deprecated
public final class ICBMClassicHelpers
{

    public static IExplosive getExplosive(ItemStack stack)
    {
        if (stack.hasCapability(ICBMClassicAPI.EXPLOSIVE_CAPABILITY, null))
        {
            return stack.getCapability(ICBMClassicAPI.EXPLOSIVE_CAPABILITY, null);
        }
        return null;
    }

    /**
     * Gets the {@link IGPSData} capability from an {@link ItemStack}
     * @param stack to access
     * @return data if present, or null otherwise
     */
    public static IGPSData getGPSData(ItemStack stack) {
        if(stack.hasCapability(ICBMClassicAPI.GPS_CAPABILITY, null)) {
            return stack.getCapability(ICBMClassicAPI.GPS_CAPABILITY, null);
        }
        return null;
    }
}
