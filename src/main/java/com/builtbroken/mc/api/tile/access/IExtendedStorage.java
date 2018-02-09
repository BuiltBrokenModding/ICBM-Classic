package com.builtbroken.mc.api.tile.access;

import net.minecraft.item.ItemStack;

/**
 * Applied to blocks that store items in stacks above 64 and as one large collective of items
 *
 * @author DarkGuardsman
 */
@Deprecated //Being replaced with a different solution
public interface IExtendedStorage
{
	ItemStack addStackToStorage(ItemStack stack);
}
