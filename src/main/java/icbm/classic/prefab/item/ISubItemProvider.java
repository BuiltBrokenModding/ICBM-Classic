package icbm.classic.prefab.item;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;

/**
 * Replacement for 1.12 {@code Item#getSubItems(CreativeTabs, NonNullList)}. Items with several variants
 * (explosive tiers, missiles, grenades...) implement this so the creative tab and JEI list every variant.
 * Items that don't implement it show a single default stack.
 */
public interface ISubItemProvider
{
    void getSubItems(NonNullList<ItemStack> items);
}
// PORTED
