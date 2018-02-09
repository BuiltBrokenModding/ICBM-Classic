package com.builtbroken.mc.api;

import net.minecraft.item.ItemStack;

/**
 * Allows filtering inventory searches to return items that are desired rather than all
 *
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 11/15/2016.
 */
public interface IInventoryFilter
{
    /**
     * Checks if the item is valid, ignores stack size
     *
     * @param stack - stack to compare
     * @return true if the stack is in the filter
     */
    boolean isStackInFilter(ItemStack stack);
}
