package icbm.classic.content.blocks.emptower;

import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.content.blocks.launcher.cruise.TileCruiseLauncher;
import icbm.classic.lib.energy.system.EnergySystem;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;

public class EmpTowerInventory extends ItemStackHandler
{
    public static final int SLOT_ENERGY = 0;

    public ItemStack getEnergySlot() {
        return getStackInSlot(SLOT_ENERGY);
    }

    @Override
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate)
    {
        return isItemValid(slot, stack) ? super.insertItem(slot, stack, simulate) : stack;
    }

    @Override
    public boolean isItemValid(int slot, @Nonnull ItemStack stack)
    {
        return slot == SLOT_ENERGY && EnergySystem.isEnergyItem(stack, null);
    }
}
