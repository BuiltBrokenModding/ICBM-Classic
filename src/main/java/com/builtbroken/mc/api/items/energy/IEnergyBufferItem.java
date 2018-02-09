package com.builtbroken.mc.api.items.energy;

import net.minecraft.item.ItemStack;

/**
 * Applied to items that store energy
 *
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 1/4/2015
 */
public interface IEnergyBufferItem //TODO move to node system
{

    /**
     * Get the amount of energy currently stored in the item.
     */
    int getEnergy(ItemStack theItem);

    /**
     * Get the max amount of energy that can be stored in the item.
     */
    int getEnergyCapacity(ItemStack theItem);

    /**
     * Sets the amount of energy in the ItemStack. Use recharge or discharge instead of calling this
     * to be safer!
     *
     * @param itemStack - the ItemStack.
     * @param energy    - Amount of electrical energy.
     */
    void setEnergy(ItemStack itemStack, int energy);
}
