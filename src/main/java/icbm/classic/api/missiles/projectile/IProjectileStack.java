package icbm.classic.api.missiles.projectile;

import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;

/**
 * Version of capability to apply specific to ItemStacks for additional features
 * <p>
 * Items can either be the projectile directly or a container of a projectile(s)
 */
public interface IProjectileStack<E extends Entity>
{
    /**
     * Gets data for the projectile
     *
     * @return data, should come from {@link icbm.classic.api.ICBMClassicAPI#PROJECTILE_DATA_REGISTRY} to allow
     * other mods to override the return value of the registry key.
     */
    @Nullable
    IProjectileData<E> getProjectileData();

    /**
     * Called to consume the projectile from the
     * stack. Usually called after the projectile is built
     * and spawned into the world.
     *
     * If this is a single projectile then return empty. If not
     * then return the container.
     *
     * @return stack to return to inventory
     */
    default ItemStack consumeProjectile(int count)
    {
        return ItemStack.EMPTY;
    }

    /**
     * Number of projectiles contained in the stack
     *
     * @return count
     */
    default int getProjectiles() {
        return 1;
    }
}
