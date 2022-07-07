package icbm.classic.api.missiles;

import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

/**
 * Applied to objects that have the ability to build missiles
 *
 * Can be applied {@link net.minecraft.item.ItemStack} to create missile items
 * or containers of missiles.
 */
public interface ICapabilityMissileBuilder
{
    /**
     * Called to generate a new missile
     * @param world to spawn inside
     * @return missile capability with contained entity
     */
    IMissile newMissile(World world);
}
