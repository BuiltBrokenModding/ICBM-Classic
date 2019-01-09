package icbm.classic.api.caps;

import net.minecraft.item.ItemStack;

/**
 * Capability for gaining access to the missile.
 * <p>
 * Keep in mind this may not be directly on the calling block.
 * It may be wrapped to the host block. Especially in the case of
 * multi-block such as the launcher base.
 * <p>
 * In theory this should just be a wrapper to the inventory. However,
 * it may do additional checks to ensure the missile is supported
 * Created by Dark(DarkGuardsman, Robert) on 1/9/19.
 */
public interface IMissileHolder
{

    /**
     * Access the missile as an itemstack
     *
     * @return
     */
    ItemStack getMissileStack();

    /**
     * Set the missile stack.
     * <p>
     * Does not validate the stack and will
     * override any checks
     *
     * @param stack
     */
    void setMissileStack(ItemStack stack);

    /**
     * Called to insert the missile into the holder
     *
     * @param stack    - stack
     * @param doInsert - true to insert, false to test
     * @return remaining stack or empty stack if taken
     */
    ItemStack insertMissileStack(ItemStack stack, boolean doInsert);

    /**
     * Is the missile support by the holder
     *
     * @param stack - stack
     * @return true if supported
     */
    boolean canSupportMissile(ItemStack stack);
}
