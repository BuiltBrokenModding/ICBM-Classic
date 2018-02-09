package com.builtbroken.mc.api.items.tools;

import com.builtbroken.mc.api.IWorldPosition;
import com.builtbroken.mc.imp.transform.vector.Location;
import net.minecraft.item.ItemStack;

/** Simple way to store a location inside an Item's NBT
 *
 *
 * Created by robert on 4/15/2015.
 */
public interface IWorldPosItem
{
    /**
     * Retrieves the location from the NBT,
     * Creates a new object each method call
     * @return Location(World, x, y, z)
     */
    Location getLocation(ItemStack stack);

    /**
     * Sets the location data in the Item's NBT
     * @param loc
     */
    void setLocation(ItemStack stack, IWorldPosition loc);

    /**
     * Used by the item to prevent access directly to the stored data. Designed
     * to prevent tiles from direct access to data.
     * @param stack - itemstack
     * @param obj - entity or tile normally, object that is access that data
     * @return true if the data can be accessed
     */
    boolean canAccessLocation(ItemStack stack, Object obj);
}
