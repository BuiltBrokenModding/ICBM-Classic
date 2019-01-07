package icbm.classic.content.machines.battery;

import net.minecraft.item.ItemStack;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.IItemHandler;

import java.util.Iterator;

/**
 *
 * Created by Dark(DarkGuardsman, Robert) on 3/21/2018.
 */
public class InventoryEnergyStorage implements IEnergyStorage, Iterable<IEnergyStorage>
{
    public final IItemHandler inventory;

    public InventoryEnergyStorage(IItemHandler inventory)
    {
        this.inventory = inventory;
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate)
    {
        int energy = 0;
        int energyLeft = maxReceive;
        for (IEnergyStorage storage : this)
        {
            int added = storage.receiveEnergy(energyLeft, simulate);
            energy += added;
            energyLeft -= added;
        }
        return energy;
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate)
    {
        int energy = 0;
        int energyToRemove = maxExtract;
        for (IEnergyStorage storage : this)
        {
            int added = storage.extractEnergy(energyToRemove, simulate);
            energy += added;
            energyToRemove -= added;
        }
        return energy;
    }

    @Override
    public int getEnergyStored()
    {
        int energy = 0;
        for (IEnergyStorage storage : this)
        {
            energy += storage.getEnergyStored();
        }
        return energy;
    }

    @Override
    public int getMaxEnergyStored()
    {
        int energy = 0;
        for (IEnergyStorage storage : this)
        {
            energy += storage.getMaxEnergyStored();
        }
        return energy;
    }

    @Override
    public boolean canExtract()
    {
        return true;
    }

    @Override
    public boolean canReceive()
    {
        return true;
    }

    @Override
    public Iterator<IEnergyStorage> iterator()
    {
        return new IESIterator(inventory);
    }

    public static class IESIterator implements Iterator<IEnergyStorage>
    {
        private final IItemHandler inventory;

        int currentSlot = -1;
        int nextSlot = 0;

        public IESIterator(IItemHandler inventory)
        {
            this.inventory = inventory;
        }

        @Override
        public boolean hasNext()
        {
            while (get(nextSlot) == null && insideLimits())
            {
                nextSlot++;
            }
            return isNextValid();
        }

        private boolean insideLimits()
        {
            return nextSlot >= 0 && nextSlot < inventory.getSlots();
        }

        private boolean isNextValid()
        {
            return insideLimits() && get(nextSlot) != null;
        }

        @Override
        public IEnergyStorage next()
        {
            currentSlot = nextSlot;
            nextSlot++;
            return get(currentSlot);
        }

        public IEnergyStorage get(int index)
        {
            ItemStack itemStack = inventory.getStackInSlot(index);
            if (!itemStack.isEmpty() && itemStack.hasCapability(CapabilityEnergy.ENERGY, null))
            {
                return itemStack.getCapability(CapabilityEnergy.ENERGY, null);
            }
            return null;
        }
    }
}
