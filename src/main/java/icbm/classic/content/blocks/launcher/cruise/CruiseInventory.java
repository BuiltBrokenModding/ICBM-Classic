package icbm.classic.content.blocks.launcher.cruise;

import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.content.blocks.launcher.base.TileLauncherBase;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;

public class CruiseInventory extends ItemStackHandler
{
    private final TileCruiseLauncher host;

    public CruiseInventory(TileCruiseLauncher host)
    {
        super(2);
        this.host = host;
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
