package icbm.classic.lib.explosive.reg;

import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.data.EntityTickFunction;
import icbm.classic.api.data.WorldEntityIntSupplier;
import icbm.classic.api.reg.IExplosiveData;
import icbm.classic.api.reg.content.IExMinecartRegistry;
import icbm.classic.content.reg.ItemReg;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IntHashMap;
import net.minecraft.util.ResourceLocation;

import java.util.HashMap;

/**
 *
 * Created by Dark(DarkGuardsman, Robert) on 1/7/19.
 */
public class ExMinecartContentReg extends ExplosiveContentRegistry implements IExMinecartRegistry
{
    private final HashMap<ResourceLocation, WorldEntityIntSupplier> fuseSetSupplierMap = new HashMap();
    private final HashMap<ResourceLocation, EntityTickFunction> fuseTickCallbackMap = new HashMap();

    private final IntHashMap<WorldEntityIntSupplier> fuseSetSupplier = new IntHashMap();
    private final IntHashMap<EntityTickFunction> fuseTickCallback = new IntHashMap();

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

    @Override
    public void lockRegistry()
    {
        if(!isLocked())
        {
            super.lockRegistry();
            fuseSetSupplierMap.forEach((regName, func) -> {
                final IExplosiveData data = ICBMClassicAPI.EXPLOSIVE_REGISTRY.getExplosiveData(regName);
                if (data != null)
                {
                    fuseSetSupplier.addKey(data.getRegistryID(), func);
                }
            });
            fuseTickCallbackMap.forEach((regName, func) -> {
                final IExplosiveData data = ICBMClassicAPI.EXPLOSIVE_REGISTRY.getExplosiveData(regName);
                if (data != null)
                {
                    fuseTickCallback.addKey(data.getRegistryID(), func);
                }
            });
        }
        else
            throw new RuntimeException(this + ": Registry was locked twice!");
    }

    @Override
    public void setFuseSupplier(ResourceLocation exName, WorldEntityIntSupplier fuseTimer)
    {
        fuseSetSupplierMap.put(exName, fuseTimer);
    }

    @Override
    public void setFuseTickListener(ResourceLocation exName, EntityTickFunction function)
    {
        fuseTickCallbackMap.put(exName, function);
    }

    @Override
    public void tickFuse(Entity entity, int ticksExisted, int explosiveID)
    {
        final EntityTickFunction function = fuseTickCallback.lookup(explosiveID);
        if (function != null)
        {
            function.onTick(entity, ticksExisted);
        }
    }

    @Override
    public int getFuseTime(Entity entity, int explosiveID)
    {
        final WorldEntityIntSupplier function = fuseSetSupplier.lookup(explosiveID);
        if (function != null)
        {
            return function.get(entity);
        }
        return 100;
    }
}
