package icbm.classic.api.missiles;


import icbm.classic.api.data.IWorldPosition;
import icbm.classic.api.explosion.responses.BlastResponse;
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
    BlastResponse doExplosion();

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
        if (!hasExploded() && !doExplosion().state.good)
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
        final ItemStack stack = toStack();
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
     * Sets the missile targeting data
     * @param data defining the target and any specialized impact settings
     */
    void setTargetData(IMissileTarget data);

    /**
     * Tells the missile to fly towards the specific target
     */
    void launch();
}
