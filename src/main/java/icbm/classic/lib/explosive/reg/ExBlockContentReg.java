package icbm.classic.lib.explosive.reg;

import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.data.BlockActivateFunction;
import icbm.classic.api.data.LevelPosIntSupplier;
import icbm.classic.api.data.LevelTickFunction;
import icbm.classic.api.reg.ExplosiveType;
import icbm.classic.api.reg.content.IExBlockRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.IntHashMap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.HashMap;

/**
 * Created by Dark(DarkGuardsman, Robert) on 1/7/19.
 */
public class ExBlockContentReg extends ExplosiveContentRegistry implements IExBlockRegistry {
    private final HashMap<ResourceLocation, LevelPosIntSupplier> fuseSetSupplierMap = new HashMap();
    private final HashMap<ResourceLocation, LevelTickFunction> fuseTickCallbackMap = new HashMap();
    private final HashMap<ResourceLocation, BlockActivateFunction> blockActivationCallbackMap = new HashMap();

    private final IntHashMap<LevelPosIntSupplier> fuseSetSupplier = new IntHashMap();
    private final IntHashMap<LevelTickFunction> fuseTickCallback = new IntHashMap();
    private final IntHashMap<BlockActivateFunction> blockActiviationCallback = new IntHashMap();

    public ExBlockContentReg() {
        super(ICBMClassicAPI.EX_BLOCK);
    }

    @Override
    public ItemStack getDeviceStack(ResourceLocation regName) {
        ExplosiveType ex = getExplosive(regName);
        if (ex != null) {
            return new ItemStack(BlockReg.blockExplosive, 1, ex.getRegistryID());
        }
        return null;
    }

    @Override
    public void lockRegistry() {
        if (!isLocked()) {
            super.lockRegistry();
            fuseSetSupplierMap.forEach((regName, func) -> {
                final ExplosiveType data = ICBMClassicAPI.EXPLOSIVE_REGISTRY.getExplosiveData(regName);
                if (data != null) {
                    fuseSetSupplier.addKey(data.getRegistryID(), func);
                }
            });
            fuseTickCallbackMap.forEach((regName, func) -> {
                final ExplosiveType data = ICBMClassicAPI.EXPLOSIVE_REGISTRY.getExplosiveData(regName);
                if (data != null) {
                    fuseTickCallback.addKey(data.getRegistryID(), func);
                }
            });
            blockActivationCallbackMap.forEach((regName, func) -> {
                final ExplosiveType data = ICBMClassicAPI.EXPLOSIVE_REGISTRY.getExplosiveData(regName);
                if (data != null) {
                    blockActiviationCallback.addKey(data.getRegistryID(), func);
                }
            });
        } else
            throw new RuntimeException(this + ": Registry was locked twice!");
    }

    @Override
    public void setFuseSupplier(ResourceLocation exName, LevelPosIntSupplier fuseTimer) {
        fuseSetSupplierMap.put(exName, fuseTimer);
    }

    @Override
    public void setFuseTickListener(ResourceLocation exName, LevelTickFunction function) {
        fuseTickCallbackMap.put(exName, function);
    }

    @Override
    public void setActivationListener(ResourceLocation exName, BlockActivateFunction function) {
        blockActivationCallbackMap.put(exName, function);
    }

    @Override
    public void tickFuse(Level level, double posX, double posY, double posZ, int ticksExisted, int explosiveID) {
        final LevelTickFunction function = fuseTickCallback.lookup(explosiveID);
        if (function != null) {
            function.onTick(world, posX, posY, posZ, ticksExisted);
        }
    }

    @Override
    public int getFuseTime(Level level, double posX, double posY, double posZ, int explosiveID) {
        final LevelPosIntSupplier function = fuseSetSupplier.lookup(explosiveID);
        if (function != null) {
            return function.get(world, posX, posY, posZ);
        }
        return 100;
    }
}
