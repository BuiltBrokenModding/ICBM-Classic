package icbm.classic.api.missiles;

import net.minecraft.world.level.Level;

/**
 * Applied to objects that have the ability to build missiles
 * <p>
 * Can be applied {@link net.minecraft.world.item.ItemStack} to create missile items
 * or containers of missiles.
 */
public interface ICapabilityMissileBuilder {
    /**
     * Unique id to represent the missile and it's subtype
     * <p>
     * This is often used by other mods to represent the missile
     * without calling {@link #newMissile(Level)} to check type
     * <p>
     * Example: 'icbmclassic:missile.redmatter'
     *
     * @return unique id
     */
    String getMissileId();

    /**
     * Called to generate a new missile
     *
     * @param level to spawn inside
     * @return missile capability with contained entity
     */
    IMissile newMissile(Level level);
}
