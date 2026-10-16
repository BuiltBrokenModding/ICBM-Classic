package icbm.classic.api.missiles.projectile;

import icbm.classic.api.actions.data.EntityActionTypes;
import icbm.classic.api.data.meta.ITypeTaggable;
import icbm.classic.api.reg.obj.IBuildableObject;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Information about a projectile, usually wrapper for an entity or entity spawn system. Provides
 * the type of projectile, what systems it can work with, and how to place the projectile into the world.
 *
 * Projectile system exists as a way to abstract entity from system. Allowing content, such as cluster, to
 * list supported projectiles and store them in a safe data structure. Where the entity can be spawned when
 * needed without knowing what it is or how it works.
 *
 * @param <E> created from the projectile data
 *
 * @deprecated being replaced with {@link icbm.classic.api.actions.IActionData} and {@link EntityActionTypes#PROJECTILE}
 * as current solution isn't flexible enough and results in odd spawning when {@link #onEntitySpawned(Entity, Entity, InteractionHand)} is invoked. It
 * also gives caller too much visibility into what is spawned. When it should just provide starting information via {@link icbm.classic.api.actions.data.IActionFieldProvider}
 * then let the spawn entity use what it needs. Plus this makes it far easier for reuse of common actions and provide implemetation for other mods.
 * As the majority of projects can be implemented as ActionSpawnEntity((world, pos, provider) -> new Entity(world, pos, provider)). Dropping
 * need for a custom implementation.
 */
public interface IProjectileData<E extends Entity>  extends IBuildableObject, ITypeTaggable {

    //TODO add a way to check size, this way we can limit weapon systems from using extremely large projectiles if too small... share size data with radar

    /**
     * Called to generate a new projectile entity
     *
     * @param world to spawn inside
     * @param allowItemPickup true to allow entity to be collected, such as picking up arrows
     * @return entity to spawn
     */
    E newEntity(Level world, boolean allowItemPickup);

    /**
     * Called after the entity has been added to the world. Useful
     * for adding riding entities or customize based on position.
     *
     * Usually at this point the entity has it's set position, motion, and rotations. It
     * is recommended to not change this information as it can break interactions.
     *
     * @param entity created and added to the world
     * @param source that created the entity, may not always be present
     * @param hand used to spawn entity, for non-humanoid this will always be main hand
     */
    default void onEntitySpawned(@Nonnull E entity, @Nullable Entity source, @Nullable InteractionHand hand) {

    }

}
// PORTED
