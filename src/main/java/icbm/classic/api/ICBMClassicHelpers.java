package icbm.classic.api;

import icbm.classic.ICBMClassic;
import icbm.classic.api.caps.IMissile;
import icbm.classic.api.reg.IExplosiveData;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
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
        System.out.println("ICBMClassicAPI: Error - Failed to locate explosive for ID[" + explosive + "] this may cause unexpected logic");
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
        IExplosiveData data = ICBMClassicAPI.EXPLOSIVE_REGISTRY.getExplosiveData(name);
        if (data != null)
        {
            return data;
        }
        System.out.println("ICBMClassicAPI: Error - Failed to locate explosive for Name[" + name + "] this may cause unexpected logic");
        return returnNull ? null : ExplosiveRefs.CONDENSED;
    }

    /**
     * Checks if the entity is a missile
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

    @Deprecated //Will be placed in a registry/handler
    public static boolean hasEmpHandler(IBlockState iBlockState)
    {
        return false; //TODO implement
    }
}
