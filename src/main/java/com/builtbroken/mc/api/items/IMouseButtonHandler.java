package com.builtbroken.mc.api.items;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

/**
 * Applied to items that want to received mouse button events
 *
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 11/18/2016.
 */
public interface IMouseButtonHandler
{
    /**
     * Called when the mouse click event is received
     * <p>
     * Is called both sides
     *
     * @param stack  - item
     * @param player - who is holding the item
     * @param button - button on the mouse that was changed
     *               -1 is scroll wheel movement but is never passed into this method
     *               0 is left
     *               1 is right
     *               2 is scroll wheel
     * @param state  - state that button is in
     */
    void mouseClick(ItemStack stack, EntityPlayer player, int button, boolean state);

    /**
     * Should the event be canceled for the mouse click to prevent
     * normal MC actions?
     * <p>
     * this is only called client side currently as user mouse & keyboard events
     * are only fired on one side.
     *
     * @param stack  - stack
     * @param player - player
     * @return true if yes
     */
    default boolean shouldCancelMouseEvent(ItemStack stack, EntityPlayer player, int button, boolean state)
    {
        return button == 0;
    }
}
