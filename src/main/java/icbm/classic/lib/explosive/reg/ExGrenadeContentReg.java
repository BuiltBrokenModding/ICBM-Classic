package icbm.classic.lib.explosive.reg;

import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.data.EntityTickFunction;
import icbm.classic.api.data.LevelEntityIntSupplier;
import icbm.classic.api.reg.ExplosiveType;
import icbm.classic.api.reg.content.IExGrenadeRegistry;
import icbm.classic.world.IcbmItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Dark(DarkGuardsman, Robert) on 1/7/19.
 */
public class ExGrenadeContentReg extends ExplosiveContentRegistry implements IExGrenadeRegistry {

    private final HashMap<ResourceLocation, LevelEntityIntSupplier> fuseSetSupplierMap = new HashMap<>();
    private final HashMap<ResourceLocation, EntityTickFunction> fuseTickCallbackMap = new HashMap<>();

    private final Map<ExplosiveType, LevelEntityIntSupplier> fuseSetSupplier = new HashMap<>();
    private final Map<ExplosiveType, EntityTickFunction> fuseTickCallback = new HashMap<>();

    public ExGrenadeContentReg() {
        super(ICBMClassicAPI.EX_GRENADE);
    }

    @Override
    public ItemStack getDeviceStack(ResourceLocation regName) {
        ExplosiveType ex = getExplosive(regName);
        if (ex != null) {
            return new ItemStack(IcbmItems.GRENADE, 1, ex.getRegistryID());
        }
        return null;
    }

    @Override
    public void lockRegistry() {
        if (!isLocked()) {
            super.lockRegistry();
            fuseSetSupplierMap.forEach((regName, func) -> {
                ExplosiveType type = ICBMClassicAPI.EXPLOSIVE_REGISTRY.getExplosiveData(regName);
                if (type != null) {
                    fuseSetSupplier.put(type, func);
                }
            });
            fuseTickCallbackMap.forEach((regName, func) -> {
                ExplosiveType type = ICBMClassicAPI.EXPLOSIVE_REGISTRY.getExplosiveData(regName);
                if (type != null) {
                    fuseTickCallback.put(type, func);
                }
            });
        } else {
            throw new RuntimeException(this + ": Registry was locked twice!");
        }
    }

    @Override
    public void setFuseSupplier(ResourceLocation exName, LevelEntityIntSupplier fuseTimer) {
        fuseSetSupplierMap.put(exName, fuseTimer);
    }

    @Override
    public void setFuseTickListener(ResourceLocation exName, EntityTickFunction function) {
        fuseTickCallbackMap.put(exName, function);
    }

    @Override
    public void tickFuse(Entity entity, ExplosiveType type, int ticksExisted) {
        final EntityTickFunction function = fuseTickCallback.get(type);
        if (function != null) {
            function.onTick(entity, ticksExisted);
        }
    }

    @Override
    public int getFuseTime(Entity entity, ExplosiveType data) {
        final LevelEntityIntSupplier function = fuseSetSupplier.get(data);
        if (function != null) {
            return function.get(entity);
        }
        return 100;
    }
}
