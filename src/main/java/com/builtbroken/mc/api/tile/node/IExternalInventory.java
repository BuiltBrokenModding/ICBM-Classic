package com.builtbroken.mc.api.tile.node;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Applied to objects that are used as inventory implementation external
 * from the core object.
 *
 * @author DarkGuardsman
 */
public interface IExternalInventory extends IInventory
{
    /**
     * Gets the inventory array. ForgeDirection.UNKOWN must return all sides
     */
    Collection<ItemStack> getContainedItems();

    /**
     * Deletes all the items in the inventory
     */
    void clear();

    /**
     * Checks if the inventory is empty
     *
     * @return true if no items are contained
     */
    default boolean isEmpty()
    {
        return getContainedItems().size() == 0;
    }

    /**
     * Checks if the inventory is 100% full
     *
     * @return true if all slots are filled
     */
    default boolean isFull()
    {
        if (getContainedItems().size() >= getSizeInventory())
        {
            for (int i = 0; i < getSizeInventory(); i++)
            {
                int maxSpace = Math.min(getStackInSlot(i).getMaxStackSize(), getInventoryStackLimit());
                int space = maxSpace - getStackInSlot(i).getCount();
                if (space > 0)
                {
                    return false;
                }
            }
        }
        return false;
    }

    /**
     * Gets all slots  that are full
     * <p>
     * If you use VoltzEngine as a hard dep just
     * wrapper this to (@link InventoryUtility#getFilledSlots(IInventory)}
     *
     * @return list of slots
     */
    ArrayList<Integer> getFilledSlots();

    /**
     * Gets all slots  that are empty
     *
     * @return list of slots
     */
    ArrayList<Integer> getEmptySlots();

    /**
     * Gets all slots  that have room to insert items
     *
     * @return list of slots
     */
    ArrayList<Integer> getSlotsWithSpace();
}
