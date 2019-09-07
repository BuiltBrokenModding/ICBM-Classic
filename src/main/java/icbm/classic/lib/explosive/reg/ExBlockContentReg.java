package icbm.classic.lib.explosive.reg;

import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.data.BlockActivateFunction;
import icbm.classic.api.data.WorldPosIntSupplier;
import icbm.classic.api.data.WorldTickFunction;
import icbm.classic.api.reg.IExplosiveData;
import icbm.classic.api.reg.content.IExBlockRegistry;
import icbm.classic.content.reg.BlockReg;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IntHashMap;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import java.util.HashMap;

/**
 * Created by Dark(DarkGuardsman, Robert) on 1/7/19.
 */
public class ExBlockContentReg extends ExplosiveContentRegistry implements IExBlockRegistry
{
    private final HashMap<ResourceLocation, WorldPosIntSupplier> fuseSetSupplierMap = new HashMap();
    private final HashMap<ResourceLocation, WorldTickFunction> fuseTickCallbackMap = new HashMap();
    private final HashMap<ResourceLocation, BlockActivateFunction> blockActiviationCallbackMap = new HashMap();

    private final IntHashMap<WorldPosIntSupplier> fuseSetSupplier = new IntHashMap();
    private final IntHashMap<WorldTickFunction> fuseTickCallback = new IntHashMap();
    private final IntHashMap<BlockActivateFunction> blockActiviationCallback = new IntHashMap();

    public ExBlockContentReg()
    {
        super(ICBMClassicAPI.EX_BLOCK);
    }

    @Override
    public ItemStack getDeviceStack(ResourceLocation regName)
    {
        IExplosiveData ex = getExplosive(regName);
        if(ex != null)
        {
            return new ItemStack(BlockReg.blockExplosive, 1, ex.getRegistryID());
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
            blockActiviationCallbackMap.forEach((regName, func) -> {
                final IExplosiveData data = ICBMClassicAPI.EXPLOSIVE_REGISTRY.getExplosiveData(regName);
                if (data != null)
                {
                    blockActiviationCallback.addKey(data.getRegistryID(), func);
                }
            });
        }
        else
            throw new RuntimeException(this + ": Registry was locked twice!");
    }

    @Override
    public void setFuseSupplier(ResourceLocation exName, WorldPosIntSupplier fuseTimer)
    {
        fuseSetSupplierMap.put(exName, fuseTimer);
    }

    @Override
    public void setFuseTickListener(ResourceLocation exName, WorldTickFunction function)
    {
        fuseTickCallbackMap.put(exName, function);
    }

    @Override
    public void setActivationListener(ResourceLocation exName, BlockActivateFunction function)
    {
        blockActiviationCallbackMap.put(exName, function);
    }

    @Override
    public void tickFuse(World world, double posX, double posY, double posZ, int ticksExisted, int explosiveID)
    {
        final WorldTickFunction function = fuseTickCallback.lookup(explosiveID);
        if (function != null)
        {
            function.onTick(world, posX, posY, posZ, ticksExisted);
        }
    }

    @Override
    public int getFuseTime(World world, double posX, double posY, double posZ, int explosiveID)
    {
        final WorldPosIntSupplier function = fuseSetSupplier.lookup(explosiveID);
        if (function != null)
        {
            return function.get(world, posX, posY, posZ);
        }
        return 100;
    }
}
