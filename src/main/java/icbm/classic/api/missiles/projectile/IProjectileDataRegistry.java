package icbm.classic.api.missiles.projectile;

import icbm.classic.api.reg.obj.IBuilderRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public interface IProjectileDataRegistry extends IBuilderRegistry<IProjectileData> {

    /**
     * Registers a conversion for the given item and matching state. Meant for vanilla conversions
     * or integration with mods that have simple item key to projectile data.
     *
     * When spawning a projectile a list of builders is used. First builder matching the stack will
     * result in that projectile data being used. It is recommended to use {@link IProjectileStack}
     * for overriding this behavior.
     *
     * @param itemStack to register with, only item is saved. Stack data is used for sorting
     * @param matcher to reduce down to a specific stack
     * @param projectileDataKey to use for projectile building
     */
    default void registerItemStackConversation(ItemStack itemStack, Function<ItemStack, Boolean> matcher, ResourceLocation projectileDataKey) {
        registerItemStackConversation(itemStack, (stack) ->  matcher.apply(stack) ? this.build(projectileDataKey) : null);
    }

    /**
     * Registers a conversion for the given item and matching state. Meant for vanilla conversions
     * or integration with mods that have simple item key to projectile data.
     *
     * When spawning a projectile a list of builders is used. First builder matching the stack will
     * result in that projectile data being used. It is recommended to use {@link IProjectileStack}
     * for overriding this behavior.
     *
     * @param itemStack to register with, only item is saved. Stack data is used for sorting
     * @param builder to use, return null if the stack isn't supported
     */
    void registerItemStackConversation(ItemStack itemStack, Function<ItemStack, IProjectileData> builder);

    /**
     * Spawns a projectile
     *
     * @param key to use
     * @param world to spawn into
     * @param x axis
     * @param y axis
     * @param z axis
     * @param allowItemPickup to allow collecting entity, such as picking up arrows
     * @param source to optionally provide, may be used for damage origin or other data during spawning
     *
     * @return entity or null if failed to spawn or data was missing
     */
    default Entity spawnProjectile(ResourceLocation key, World world, double x, double y, double z, boolean allowItemPickup, @Nullable Entity source) {
        //TODO return spawn event with more details
        //TODO consider adding a settings object in place of allowItemPickup
        return spawnProjectile(key, world, x, y, z, source, allowItemPickup, null);
    }

    /**
     * Spawns a projectile
     *
     * @param key to use
     * @param world to spawn into
     * @param x axis
     * @param y axis
     * @param z axis
     * @param source to optionally provide, may be used for damage origin or other data during spawning
     * @param preSpawnCallback optional callback to set additional details on the entity, such as velocity
     *
     * @return entity or null if failed to spawn or data was missing
     */
    Entity spawnProjectile(ResourceLocation key, World world, double x, double y, double z,
                                   @Nullable Entity source, boolean allowItemPickup,
                                   @Nullable Consumer<Entity> preSpawnCallback);

    /**
     * Spawns a projectile
     *
     * @param item to attempt to spawn, will try to find matching projectile data... if not will spawn an EntityItem
     * @param world to spawn into
     * @param x axis
     * @param y axis
     * @param z axis
     * @param source to optionally provide, may be used for damage origin or other data during spawning
     * @param preSpawnCallback optional callback to set additional details on the entity, such as velocity
     *
     * @return entity or null if failed to spawn or data was missing
     */
    Entity spawnProjectile(ItemStack item, World world, double x, double y, double z, @Nullable Entity source,
                           boolean allowItemPickup, @Nullable Consumer<Entity> preSpawnCallback);

    /**
     * Spawns a projectile
     *
     * @param data to use during spawning
     * @param world to spawn into
     * @param x axis
     * @param y axis
     * @param z axis
     * @param source to optionally provide, may be used for damage origin or other data during spawning
     * @param preSpawnCallback optional callback to set additional details on the entity, such as velocity
     *
     * @return entity or null if failed to spawn or data was missing
     */
    <E extends Entity> E spawnProjectile(IProjectileData<E> data, World world, double x, double y, double z, @Nullable Entity source,
                           boolean allowItemPickup, @Nullable Consumer<E> preSpawnCallback);
}
