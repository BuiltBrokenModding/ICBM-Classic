package icbm.classic.api.missiles.projectile;

import icbm.classic.api.missiles.cause.IMissileSource;
import net.minecraft.entity.Entity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Should only be applied to projectiles that act as throwable items/weapons. When implementing should be combined
 * with {@link IProjectileData} with type {@link ProjectileType#TYPE_THROWABLE}. This way systems can spawn
 * the projectile and understand it can be thrown with a fixed velocity. Without the type any projectile system
 * will ignore the interface assuming a common entity parent is being used. That a subtype exists that may
 * not used the common logic.
 *
 * Projectiles that have their own power source should use other interfaces. Leaving the default logic to act
 * much like an egg or item throw.
 *
 */
public interface IProjectileThrowable<ProjectileEntity extends Entity> {
    /**
     * Called to initialize the position, rotation, and motion vector of a projectile. This is meant to be
     * used more in a throw (player, zombie) or defined strength launcher (dispenser, bow, catapult).
     *
     * This is called after the entity is created (not spawned) but before anything else is applied.
     *
     * It is expected some offset of position is needed to account for visual size of the projectile. The xyz provided
     * will be the firing starting point. This could be a player hand, entity location, block (dispenser), or anything
     * that can be imagined. If your projectile is 2 meters long but the base is at the tip (missiles do this). Then
     * additional offset may be needed to create a better visual for players.
     *
     * @param entity    the projectile created, before spawning
     * @param source    information about how the projectile was generated, this may be generic or not included (though recommended)
     * @param x         the x-coordinate to spawn the projectile
     * @param y         the y-coordinate to spawn the projectile
     * @param z         the z-coordinate to spawn the projectile
     * @param yaw       the yaw rotation to apply to the projectile
     * @param pitch     the pitch rotation to apply to the projectile
     * @param velocity  to use for generation motion vector
     * @param random    a random value to offset motion vector for added variation
     * @return true if everything is good to fire, false if something is invalid about the position (ex: spawning in wall)
     */
    boolean throwProjectile(@Nonnull ProjectileEntity entity, @Nullable IMissileSource source, double x, double y, double z, float yaw, float pitch, float velocity, float random);
}
