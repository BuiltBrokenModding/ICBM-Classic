package icbm.classic.lib.explosive.reg;

import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.ICBMClassicHelpers;
import icbm.classic.api.data.BlockActivateFunction;
import icbm.classic.api.data.WorldPosIntSupplier;
import icbm.classic.api.data.WorldTickFunction;
import icbm.classic.api.reg.IExplosiveData;
import icbm.classic.api.reg.content.IExBlockRegistry;
import icbm.classic.content.reg.BlockReg;
import icbm.classic.lib.explosive.ExplosiveStacks;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import java.util.HashMap;

/**
 * Created by Dark(DarkGuardsman, Robin) on 1/7/19.
 */
public class ExBlockContentReg extends ExplosiveContentRegistry implements IExBlockRegistry
{
    private final HashMap<ResourceLocation, WorldPosIntSupplier> fuseSetSupplierMap = new HashMap();
    private final HashMap<ResourceLocation, WorldTickFunction> fuseTickCallbackMap = new HashMap();
    private final HashMap<ResourceLocation, BlockActivateFunction> blockActivationCallbackMap = new HashMap();

    private final Int2ObjectOpenHashMap<WorldPosIntSupplier> fuseSetSupplier = new Int2ObjectOpenHashMap<>();
    private final Int2ObjectOpenHashMap<WorldTickFunction> fuseTickCallback = new Int2ObjectOpenHashMap<>();
    private final Int2ObjectOpenHashMap<BlockActivateFunction> blockActiviationCallback = new Int2ObjectOpenHashMap<>();

    public ExBlockContentReg()
    {
        super(ICBMClassicAPI.EX_BLOCK);
    }

    @Override
    public ItemStack getDeviceStack(ResourceLocation regName)
    {
        return ExplosiveStacks.create(BlockReg.blockExplosive.get(), ICBMClassicHelpers.getExplosive(regName, false));
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
            blockActivationCallbackMap.forEach((regName, func) -> {
                final IExplosiveData data = ICBMClassicAPI.EXPLOSIVE_REGISTRY.getExplosiveData(regName);
                if (data != null)
                {
                    blockActiviationCallback.put(data.getRegistryID(), func);
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
        blockActivationCallbackMap.put(exName, function);
    }

    @Override
    public void tickFuse(Level world, double posX, double posY, double posZ, int ticksExisted, int explosiveID)
    {
        final WorldTickFunction function = fuseTickCallback.get(explosiveID);
        if (function != null)
        {
            function.onTick(world, posX, posY, posZ, ticksExisted);
        }
    }

    @Override
    public int getFuseTime(Level world, double posX, double posY, double posZ, int explosiveID)
    {
        final WorldPosIntSupplier function = fuseSetSupplier.get(explosiveID);
        if (function != null)
        {
            return function.get(world, posX, posY, posZ);
        }
        return 100;
    }
}
// PORTED
