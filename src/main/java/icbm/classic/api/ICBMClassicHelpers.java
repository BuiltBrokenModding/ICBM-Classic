package icbm.classic.api;

import icbm.classic.ICBMClassic;
import icbm.classic.api.caps.IExplosive;
import icbm.classic.api.caps.IGPSData;
import icbm.classic.api.launcher.IMissileLauncher;
import icbm.classic.api.missiles.IMissile;
import icbm.classic.api.refs.ICBMExplosives;
import icbm.classic.api.reg.ExplosiveType;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Created by Dark(DarkGuardsman, Robert) on 1/7/19.
 */
public final class ICBMClassicHelpers {

    /**
     * Called to get explosive
     *
     * @param explosive
     * @return explosive desired, or default TNT
     */
    public static ExplosiveType getExplosive(int explosive, boolean returnNull) {
        ExplosiveType data = ICBMClassicAPI.EXPLOSIVE_REGISTRY.getExplosiveData(explosive);
        if (data != null) {
            return data;
        }
        if (ICBMClassic.runningAsDev) {
            ICBMClassic.logger().error("ICBMClassicAPI: Error - Failed to locate explosive for " +
                "ID[" + explosive + "] this may cause unexpected logic", new RuntimeException());
        }
        return returnNull ? null : ICBMExplosives.CONDENSED;
    }

    /**
     * Called to get explosive
     *
     * @param name - registry name of the explosive
     * @return explosive desired, or default TNT
     */
    public static ExplosiveType getExplosive(String name, boolean returnNull) {
        return getExplosive(new ResourceLocation(name), returnNull);
    }

    /**
     * Called to get explosive
     *
     * @param name - registry name of the explosive
     * @return explosive desired, or default TNT
     */
    public static ExplosiveType getExplosive(ResourceLocation name, boolean returnNull) {
        final ExplosiveType data = ICBMClassicAPI.EXPLOSIVE_REGISTRY.getExplosiveData(name);
        if (data != null) {
            return data;
        }
        System.out.println("ICBMClassicAPI: Error - Failed to locate explosive for Name[" + name + "] this may cause unexpected logic");
        return returnNull ? null : ICBMExplosives.CONDENSED;
    }

    /**
     * Checks if the entity is a missile
     *
     * @param entity
     * @return
     */
    public static boolean isMissile(Entity entity) {
        return entity != null && entity.getCapability(ICBMClassicAPI.MISSILE_CAPABILITY) != null;
    }

    public static boolean isMissile(ItemStack stack) {
        return !stack.isEmpty() && stack.getCapability(ICBMClassicAPI.MISSILE_STACK_CAPABILITY) != null;
    }

    /**
     * Helper to get the capability of {@link IMissile} from an entity
     *
     * @param entity to pull from
     * @return capability
     */
    public static IMissile getMissile(Entity entity) {
        return entity.getCapability(ICBMClassicAPI.MISSILE_CAPABILITY);
    }

    public static boolean isExplosive(Entity entity) {
        return entity != null && entity.getCapability(ICBMClassicAPI.EXPLOSIVE_CAPABILITY) != null;
    }

    public static IExplosive getExplosive(net.minecraft.world.entity.Entity entity) {
        return entity.getCapability(ICBMClassicAPI.EXPLOSIVE_CAPABILITY);
    }

    public static boolean isLauncher(BlockEntity blockEntity, Direction direction) {
        return blockEntity != null && blockEntity.hasCapability(ICBMClassicAPI.MISSILE_LAUNCHER_CAPABILITY, direction);
    }

    public static IMissileLauncher getLauncher(BlockEntity blockEntity, Direction direction) {
        return blockEntity.getCapability(ICBMClassicAPI.MISSILE_LAUNCHER_CAPABILITY, direction);
    }

    public static IExplosive getExplosive(ItemStack stack) {
        return stack.getCapability(ICBMClassicAPI.EXPLOSIVE_CAPABILITY);
    }

    @Deprecated //Will be placed in a registry/handler
    public static boolean hasEmpHandler(BlockState blockState) {
        return false; //TODO implement
    }

    /**
     * Gets the {@link IGPSData} capability from an {@link ItemStack}
     *
     * @param stack to access
     * @return data if present, or null otherwise
     */
    public static IGPSData getGPSData(ItemStack stack) {
        return stack.getCapability(ICBMClassicAPI.GPS_CAPABILITY);
    }
}
