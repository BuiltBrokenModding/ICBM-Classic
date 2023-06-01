package icbm.classic.api.missiles.projectile;

import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

/**
 * Version of capability to apply specific to ItemStacks for additional features
 * <p>
 * Items can either be the projectile directly or a container of missile(s)
 */
public interface IProjectileStack
{
    // default types

    /** Applied to types that spawn an entity in place of a projectile object... useful for spawn eggs */
    ProjectileType TYPE_ENTITY = new ProjectileType(new ResourceLocation("icbmclassic","entity"), null);
    /** Default type, anything that is projectile like... this includes snowballs and arrows */
    ProjectileType TYPE_PROJECTILE = new ProjectileType(new ResourceLocation("icbmclassic","projectile"), null);
    /** Missiles that use the capability {@link icbm.classic.api.missiles.IMissile} */
    ProjectileType TYPE_MISSILE = new ProjectileType(new ResourceLocation("icbmclassic","missile"), TYPE_PROJECTILE);
    /** Projectiles that are explosive and use the capability {@link icbm.classic.api.caps.IExplosive} */
    ProjectileType TYPE_BOMB = new ProjectileType(new ResourceLocation("icbmclassic","bomb"), TYPE_PROJECTILE);
    /** Default type to return */
    ProjectileType[] TYPE_DEFAULT = new ProjectileType[]{TYPE_PROJECTILE};

    //TODO add a way to check size, this way we can limit weapon systems from using extremely large projectiles if too small... share size data with radar

    /**
     * Unique name to represent the projectile, subtype, and unique configurations.
     *
     * Not everything needs to be encoded but enough to tell the projectile apart
     * from other stacks. As this information can be useful for display to users
     * or just in general for debug.
     *
     * Common usage of this is display in tooltips or in user interfaces. As the
     * stack alone may not inform the user of what is contained. This is especially
     * in cases where the stack is shared or has customization.
     *
     * Examples:
     *      'icbmclassic:missile.redmatter' -> 'domain:entity.explosive'
     *      'icbmclassic:arrow.poison'      -> 'domain:entity.type'
     *      'icbmclassic:arrow.poison.x2'   -> 'domain:entity.type.multiplier'
     *
     * Even with the above examples other ways of display this information will be
     * provided. Such as showing user defined customizations or unique settings.
     *
     * For external callers, do not depend on the name or split. As this may
     * not stay consistent between addons. As well could change between updates
     * as better naming methods are implement. Instead, use things like {@link #getTypes()}
     * or other capability information.
     *
     * @return unique id, can be used for translations
     */
    ResourceLocation getName();

    /**
     * Type(s) of projectile, used to check if a projectile can work
     * with other systems.
     *
     * A projectile can be valid for more than 1 type. Good example
     * is things like missiles which are both {@link #TYPE_MISSILE}
     * and {@link #TYPE_BOMB} if they contain an explosive.
     *
     * @return type
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
     * @return entity to spawn
     */
    Entity newEntity(World world);

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
