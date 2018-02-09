package com.builtbroken.mc.prefab.gui.slot;

import com.builtbroken.mc.framework.energy.UniversalEnergySystem;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class SlotEnergyItem extends Slot
{
	public SlotEnergyItem(IInventory inv, int par3, int par4, int par5)
	{
		super(inv, par3, par4, par5);
	}

    @Override
    public boolean isItemValid(ItemStack compareStack)
    {
        return UniversalEnergySystem.isHandler(compareStack, null);
    }

}
