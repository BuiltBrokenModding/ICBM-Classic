package icbm.classic.api.missiles.projectile;

import icbm.classic.api.missiles.parts.IBuildableObject;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Information about a projectile, usually wrapper for an entity or entity spawn system. Provides
 * the type of projectile, what systems it can work with, and how to place the projectile into the world.
 *
 * @param <E> created
 */
public interface IProjectileData<E extends Entity>  extends IBuildableObject {


    /** Default type to return */
    ProjectileType[] TYPE_DEFAULT = new ProjectileType[]{ProjectileType.TYPE_PROJECTILE};

    //TODO add a way to check size, this way we can limit weapon systems from using extremely large projectiles if too small... share size data with radar

    /**
     * Type(s) of projectile, used to check if a projectile can work
     * with other systems.
     *
     * A projectile can be valid for more than 1 type. Good example
     * is things like missiles which are both {@link ProjectileType#TYPE_MISSILE}
     * and {@link ProjectileType#TYPE_BOMB} if they contain an explosive.
     *
     * @return type(s)
     */
    default ProjectileType[] getTypes() {
        return TYPE_DEFAULT;
    }

    /**
     * Checks if the projectile is of matching type. Some types
     * are consider to be sub-types of others. Such as missiles
     * being a type of projectile.
     *
     * @param type to check
     * @return true if is valid
     */
    default boolean isType(ProjectileType type) {
        for(ProjectileType projectileType : getTypes()) {
            if(projectileType.isValidType(type)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Called to generate a new projectile entity
     *
     * @param world to spawn inside
     * @param allowItemPickup true to allow entity to be collected, such as picking up arrows
     * @return entity to spawn
     */
    E newEntity(World world, boolean allowItemPickup); //TODO consider moving an a spawnAction recycling blastAction logic

    /**
     * Called after the entity has been added to the world. Useful
     * for adding riding entities or customize based on position.
     *
     * Usually at this point the entity has it's set position, motion, and rotations. It
     * is recommended to not change this information as it can break interactions.
     *
     * @param entity created and added to the world
     * @param source that created the entity, may not always be present
     */
    default void onEntitySpawned(@Nonnull E entity, @Nullable Entity source) { //TODO consider moving an a spawnAction

    }
}
