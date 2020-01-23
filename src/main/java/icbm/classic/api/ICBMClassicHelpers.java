package icbm.classic.api;

import icbm.classic.ICBMClassic;
import icbm.classic.api.caps.IExplosive;
import icbm.classic.api.caps.IMissile;
import icbm.classic.api.caps.IMissileLauncher;
import icbm.classic.api.reg.IExplosiveData;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;

/**
 * Created by Dark(DarkGuardsman, Robert) on 1/7/19.
 */
public final class ICBMClassicHelpers
{

    /**
     * Called to get explosive
     *
     * @param explosive
     * @return explosive desired, or default TNT
     */
    public static IExplosiveData getExplosive(int explosive, boolean returnNull)
    {
        IExplosiveData data = ICBMClassicAPI.EXPLOSIVE_REGISTRY.getExplosiveData(explosive);
        if (data != null)
        {
            return data;
        }
        if(ICBMClassic.runningAsDev)
        {
            ICBMClassic.logger().error("ICBMClassicAPI: Error - Failed to locate explosive for " +
                    "ID[" + explosive + "] this may cause unexpected logic", new RuntimeException());
        }
        return returnNull ? null : ExplosiveRefs.CONDENSED;
    }

    /**
     * Called to get explosive
     *
     * @param name - registry name of the explosive
     * @return explosive desired, or default TNT
     */
    public static IExplosiveData getExplosive(String name, boolean returnNull)
    {
        return getExplosive(new ResourceLocation(name), returnNull);
    }

    /**
     * Called to get explosive
     *
     * @param name - registry name of the explosive
     * @return explosive desired, or default TNT
     */
    public static IExplosiveData getExplosive(ResourceLocation name, boolean returnNull)
    {
        final IExplosiveData data = ICBMClassicAPI.EXPLOSIVE_REGISTRY.getExplosiveData(name);
        if (data != null)
        {
            return data;
        }
        System.out.println("ICBMClassicAPI: Error - Failed to locate explosive for Name[" + name + "] this may cause unexpected logic");
        return returnNull ? null : ExplosiveRefs.CONDENSED;
    }

    /**
     * Checks if the entity is a missile
     *
     * @param entity
     * @return
     */
    public static boolean isMissile(Entity entity)
    {
        return entity != null && entity.hasCapability(ICBMClassicAPI.MISSILE_CAPABILITY, null);
    }

    public static IMissile getMissile(Entity entity)
    {
        return entity.getCapability(ICBMClassicAPI.MISSILE_CAPABILITY, null);
    }

    public static boolean isExplosive(Entity entity)
    {
        return entity != null && entity.hasCapability(ICBMClassicAPI.EXPLOSIVE_CAPABILITY, null);
    }

    public static IExplosive getExplosive(Entity entity)
    {
        return entity.getCapability(ICBMClassicAPI.EXPLOSIVE_CAPABILITY, null);
    }

    public static boolean isLauncher(TileEntity tileEntity, EnumFacing side)
    {
        return tileEntity != null && tileEntity.hasCapability(ICBMClassicAPI.MISSILE_LAUNCHER_CAPABILITY, side);
    }

    public static IMissileLauncher getLauncher(TileEntity tileEntity, EnumFacing side)
    {
        return tileEntity.getCapability(ICBMClassicAPI.MISSILE_LAUNCHER_CAPABILITY, side);
    }

    public static IExplosive getExplosive(ItemStack stack)
    {
        if (stack.hasCapability(ICBMClassicAPI.EXPLOSIVE_CAPABILITY, null))
        {
            return stack.getCapability(ICBMClassicAPI.EXPLOSIVE_CAPABILITY, null);
        }
        return null;
    }

    @Deprecated //Will be placed in a registry/handler
    public static boolean hasEmpHandler(IBlockState iBlockState)
    {
        return false; //TODO implement
    }
}
