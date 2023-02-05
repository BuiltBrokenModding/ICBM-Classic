package icbm.classic.content.blocks.launcher.base;

import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.content.reg.ItemReg;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;

public class LauncherInventory extends ItemStackHandler
{
    private final TileLauncherBase host;

    public LauncherInventory(TileLauncherBase host)
    {
        super(1);
        this.host = host;
    }

    @Override
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate)
    {
        //Prevent insertion while missile is taking off
        if (host.checkForMissileInBounds())
        {
            return stack;
        }
        return isItemValid(slot, stack) ? super.insertItem(slot, stack, simulate) : stack;
    }

    @Override
    public boolean isItemValid(int slot, @Nonnull ItemStack stack)
    {
        return stack.hasCapability(ICBMClassicAPI.MISSILE_STACK_CAPABILITY, null);
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
