package icbm.classic.lib.capability.launcher;

import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.caps.IMissileHolder;
import icbm.classic.api.missiles.ICapabilityMissileStack;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandlerModifiable;


public class CapabilityMissileHolder implements IMissileHolder
{
    private final IItemHandlerModifiable inventory;
    private final int slot;

    public CapabilityMissileHolder(IItemHandlerModifiable inventory, int slot)
    {
        this.inventory = inventory;
        this.slot = slot;
    }

    @Override
    public ItemStack getMissileStack()
    {
        return inventory.getStackInSlot(slot);
    }

    @Override
    public ItemStack insertMissileStack(ItemStack stack, boolean simulate)
    {
        if(canSupportMissile(stack)) {
            return inventory.insertItem(slot, stack, simulate);
        }
        return null;
    }

    @Override
    public boolean consumeMissile() {
        if(hasMissile()) {
            final ICapabilityMissileStack missileStack = getMissileStack().getCapability(ICBMClassicAPI.MISSILE_STACK_CAPABILITY);
            if(missileStack != null) {
                inventory.setStackInSlot(slot, missileStack.consumeMissile());
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean canSupportMissile(ItemStack stack)
    {
        return inventory.isItemValid(slot, stack);
    }

    // PORT: register() (CapabilityManager storage) removed, not needed in NeoForge
}
// PORTED
