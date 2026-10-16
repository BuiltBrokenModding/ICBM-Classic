package icbm.classic.api.missiles;

import net.minecraft.world.level.Level;

/**
 * Applied to objects that have the ability to build missiles
 *
 * Can be applied {@link net.minecraft.item.ItemStack} to create missile items
 * or containers of missiles.
 */
@Deprecated
public interface ICapabilityMissileBuilder
{
    /**
     * Unique id to represent the missile and it's subtype
     *
     * This is often used by other mods to represent the missile
     * without calling {@link #newMissile(Level)} to check type
     *
     * Example: 'icbmclassic:missile.redmatter'
     *
     * @return unique id
     */
    String getMissileId();

    /**
     * Called to generate a new missile
     * @param world to spawn inside
     * @return missile capability with contained entity
     */
    IMissile newMissile(Level world);
}
// PORTED
