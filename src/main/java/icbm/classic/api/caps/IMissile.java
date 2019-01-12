package icbm.classic.api.caps;


import icbm.classic.api.IWorldPosition;
import icbm.classic.api.explosion.BlastState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;

import javax.annotation.Nullable;

/**
 * Capability added to entities to define them as missiles
 *
 * @author DarkGuardsman
 */
public interface IMissile extends IWorldPosition
{

    /**
     * Called to trigger the missile's explosion logic
     */
    BlastState doExplosion();

    /**
     * Has the missile exploded
     *
     * @return true if missile has exploded or is in the process of exploding
     */
    boolean hasExploded();

    /**
     * Called to ask the missile to blow up on next tick
     *
     * @param fullExplosion -
     *                      True will trigger a the missile's normal explosion
     *                      False will trigger a TNT explosion
     */
    default void destroyMissile(boolean fullExplosion) //TODO add reason as input data
    {
        if (!hasExploded() && !doExplosion().good)
        {
            dropMissileAsItem();
        }
        //TODO trigger destroy event
    }

    /**
     * Drops the specified missile as an item.
     */
    default void dropMissileAsItem()
    {
        ItemStack stack = toStack();
        if (stack != null && !stack.isEmpty() && world() != null)
        {
            world().spawnEntity(new EntityItem(world(), x(), y(), z(), stack));
        }
    }

    @Nullable
    ItemStack toStack();

    /**
     * The amount of ticks this missile has been flying for. Returns -1 if the missile is not
     * flying.
     */
    int getTicksInAir(); //TODO maybe change to a status? onGround, inAir, preFlight, impacted


    //TODO store location and source of missile launch

    /**
     * Gets the entity that is host to this
     * missile capability.
     *
     * @return
     */
    Entity getMissileEntity();

    /**
     * Tells the missile to fly towards the specific target
     *
     * @param x          - target
     * @param y          - target
     * @param z          - target
     * @param lockHeight - height to above current to wait before turning towards target TODO change to distance for flat xz paths
     */
    void launch(double x, double y, double z, double lockHeight); //TODO add results

    /**
     * Called to launch without a target and fly strait from rotation data
     */
    void launchNoTarget(); //TODO add results
}
