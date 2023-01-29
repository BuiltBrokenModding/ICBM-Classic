package icbm.classic.lib.capability.launcher;

import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.caps.IMissileHolder;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nullable;

public class CapabilityMissileHolder implements IMissileHolder
{
    private final IItemHandler inventory;
    private final int slot;

    public CapabilityMissileHolder(IItemHandler inventory, int slot)
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
    public boolean canSupportMissile(ItemStack stack)
    {
        return inventory.isItemValid(slot, stack);
    }

    public static void register()
    {
        CapabilityManager.INSTANCE.register(IMissileHolder.class, new Capability.IStorage<IMissileHolder>()
            {
                @Nullable
                @Override
                public NBTBase writeNBT(Capability<IMissileHolder> capability, IMissileHolder instance, EnumFacing side)
                {
                    return null;
                }

                @Override
                public void readNBT(Capability<IMissileHolder> capability, IMissileHolder instance, EnumFacing side, NBTBase nbt)
                {

                }
            },
            () -> new CapabilityMissileHolder(null, 0));
    }
}
