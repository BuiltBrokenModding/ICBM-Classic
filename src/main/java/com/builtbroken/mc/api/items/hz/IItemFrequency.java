package com.builtbroken.mc.api.items.hz;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

/**
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 1/6/2017.
 */
public interface IItemFrequency
{
    /**
     * Gets the frequency this item broadcasts information on
     *
     * @param stack - this item
     * @return frequency
     */
    default float getBroadCastHz(ItemStack stack)
    {
        if (stack.getTagCompound() != null && stack.getTagCompound().hasKey("hz"))
        {
            return stack.getTagCompound().getFloat("hz");
        }
        return 0;
    }

    /**
     * Sets the frequency of this item
     *
     * @param stack - this item
     * @param hz    - value to set
     */
    default void setBroadCastHz(ItemStack stack, float hz)
    {
        if (stack.getTagCompound() == null)
        {
            stack.setTagCompound(new NBTTagCompound());
        }
        stack.getTagCompound().setFloat("hz", hz);
    }
}
