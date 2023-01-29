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
     * Gets the itemstack currently in the missile slot. This
     * may not actually be a missile if something bypassed checks
     *
     * @return stack in slot
     */
    ItemStack getMissileStack();

    /**
     * Called to insert the missile into the holder
     *
     * Insert will run {@link #canSupportMissile(ItemStack)} so no need
     * to call it before.
     *
     * @param stack    - stack
     * @param simulate - true to test insert, false to apply
     * @return remaining stack or empty stack if taken
     */
    ItemStack insertMissileStack(ItemStack stack, boolean simulate);

    /**
     * Is the missile supported by the holder.
     *
     * @param stack - stack
     * @return true if supported
     */
    boolean canSupportMissile(ItemStack stack);
}
