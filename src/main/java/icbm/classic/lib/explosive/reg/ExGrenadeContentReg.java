package icbm.classic.lib.explosive.reg;

import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.ICBMClassicHelpers;
import icbm.classic.api.data.EntityTickFunction;
import icbm.classic.api.data.WorldEntityIntSupplier;
import icbm.classic.api.reg.IExplosiveData;
import icbm.classic.api.reg.content.IExGrenadeRegistry;
import icbm.classic.content.reg.ItemReg;
import net.minecraft.world.entity.Entity;
import icbm.classic.lib.explosive.ExplosiveStacks;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;

/**
 *
 * Created by Dark(DarkGuardsman, Robin) on 1/7/19.
 */
public class ExGrenadeContentReg extends ExplosiveContentRegistry implements IExGrenadeRegistry
{
    private final HashMap<ResourceLocation, WorldEntityIntSupplier> fuseSetSupplierMap = new HashMap();
    private final HashMap<ResourceLocation, EntityTickFunction> fuseTickCallbackMap = new HashMap();

    private final Int2ObjectOpenHashMap<WorldEntityIntSupplier> fuseSetSupplier = new Int2ObjectOpenHashMap<>();
    private final Int2ObjectOpenHashMap<EntityTickFunction> fuseTickCallback = new Int2ObjectOpenHashMap<>();

    public ExGrenadeContentReg()
    {
        super(ICBMClassicAPI.EX_GRENADE);
    }

    @Override
    public ItemStack getDeviceStack(ResourceLocation regName)
    {
        IExplosiveData ex = ICBMClassicHelpers.getExplosive(regName, false);
        if(ex != null)
        {
            return ExplosiveStacks.create(ItemReg.itemGrenade.get(), ex);
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
                    fuseSetSupplier.put(data.getRegistryID(), func);
                }
            });
            fuseTickCallbackMap.forEach((regName, func) -> {
                final IExplosiveData data = ICBMClassicAPI.EXPLOSIVE_REGISTRY.getExplosiveData(regName);
                if (data != null)
                {
                    fuseTickCallback.put(data.getRegistryID(), func);
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
    public void tickFuse(Entity entity, IExplosiveData data, int ticksExisted)
    {
        final EntityTickFunction function = fuseTickCallback.get(data != null ? data.getRegistryID() : 0);
        if (function != null)
        {
            function.onTick(entity, ticksExisted);
        }
    }

    @Override
    public int getFuseTime(Entity entity, IExplosiveData data)
    {
        final WorldEntityIntSupplier function = fuseSetSupplier.get(data != null ? data.getRegistryID() : 0);
        if (function != null)
        {
            return function.get(entity);
        }
        return 100;
    }
}
// PORTED
