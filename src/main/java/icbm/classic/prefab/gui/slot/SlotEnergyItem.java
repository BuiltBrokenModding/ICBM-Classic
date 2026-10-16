package icbm.classic.prefab.gui.slot;

import icbm.classic.lib.energy.system.EnergySystem;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

/**
 * PORT: 1.12 IInventory slot -> item handler slot, ICBM machines use item handlers
 */
public class SlotEnergyItem extends SlotItemHandler
{
	public SlotEnergyItem(IItemHandler inv, int par3, int par4, int par5)
	{
		super(inv, par3, par4, par5);
	}

    @Override
    public boolean mayPlace(ItemStack compareStack)
    {
        return EnergySystem.getSystem(compareStack, null).canSupport(compareStack, null);
    }
}
// PORTED
