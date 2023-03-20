package icbm.classic.content.blocks.launcher.cruise;

import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.content.blocks.launcher.base.TileLauncherBase;
import icbm.classic.lib.energy.system.EnergySystem;
import icbm.classic.lib.energy.system.IEnergySystem;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import java.util.Optional;

public class CruiseInventory extends ItemStackHandler
{
    public static final int SLOT_MISSILE = 0;
    public static final int SLOT_BATTERY = 1;

    private final TileCruiseLauncher host;

    public CruiseInventory(TileCruiseLauncher host)
    {
        super(2);
        this.host = host;
    }

    public ItemStack getEnergySlot() {
        return getStackInSlot(SLOT_BATTERY);
    }

    @Override
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate)
    {
        return isItemValid(slot, stack) ? super.insertItem(slot, stack, simulate) : stack;
    }

    @Override
    public boolean isItemValid(int slot, @Nonnull ItemStack stack)
    {
        if(slot == SLOT_BATTERY) {
            return EnergySystem.isEnergyItem(stack, null);
        }
        return slot == SLOT_MISSILE && stack.hasCapability(ICBMClassicAPI.MISSILE_STACK_CAPABILITY, null);
    }

    @Override
    protected void onContentsChanged(int slot)
    {
        if (slot == 0)
        {
            host.sendDescPacket();
        }
    }
}
