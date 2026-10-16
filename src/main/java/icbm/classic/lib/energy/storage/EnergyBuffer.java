package icbm.classic.lib.energy.storage;

import icbm.classic.config.ConfigMain;
import icbm.classic.lib.energy.system.EnergySystem;
import icbm.classic.lib.energy.system.IEnergySystem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.IntTag;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.neoforged.neoforge.energy.IEnergyStorage;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * Basic implementation of energy buffer
 *
 * Created by Dark on 8/15/2015.
 */
public class EnergyBuffer implements IEnergyStorage, INBTSerializable<IntTag>
{
    private final Supplier<Integer> storageLimit;

    private EnergyChangeCallback onEnergyChanged;
    private Supplier<Boolean> canExtract;
    private Supplier<Boolean> canReceive;
    private Supplier<Integer> receiveLimit;
    private Supplier<Integer> extractLimit;

    private int energyStorage;

    public EnergyBuffer(Supplier<Integer> storageLimit)
    {
        this.storageLimit = storageLimit;
    }

    public EnergyBuffer withOnChange(EnergyChangeCallback onEnergyChanged) {
        this.onEnergyChanged = onEnergyChanged;
        return this;
    }

    public EnergyBuffer withCanExtract(Supplier<Boolean> canExtract) {
        this.canExtract = canExtract;
        return this;
    }

    public EnergyBuffer withCanReceive(Supplier<Boolean> canReceive) {
        this.canReceive = canReceive;
        return this;
    }

    public EnergyBuffer withExtractLimit(Supplier<Integer> extractLimit) {
        this.extractLimit = extractLimit;
        return this;
    }

    public EnergyBuffer withReceiveLimit(Supplier<Integer> receiveLimit) {
        this.receiveLimit = receiveLimit;
        return this;
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate)
    {
        if (!canReceive())
        {
            return 0;
        }
        final int inputEnergy = Math.min(maxReceive, Optional.ofNullable(receiveLimit).map(Supplier::get).orElse(Integer.MAX_VALUE));
        return receiveEnergyInternal(inputEnergy, simulate);
    }

    public int receiveEnergyInternal(int inputEnergy, boolean simulate)
    {
        final int maxEnergy = getMaxEnergyStored();
        final int energyStored = getEnergyStored();
        final int roomLeft = maxEnergy - energyStored;

        if (inputEnergy <= 0 || roomLeft <= 0)
        {
            return 0;
        }


        final int toAdd = Math.min(roomLeft, inputEnergy);
        if(!simulate) {
            setEnergyInternal(energyStored + toAdd, EnergyActionType.ADD);
        }
        return toAdd;
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate)
    {
        if(!canExtract()) {
            return 0;
        }
        final int desiredEnergy = Math.min(maxExtract, Optional.ofNullable(extractLimit).map(Supplier::get).orElse(Integer.MAX_VALUE));
        return extractEnergyInternal(desiredEnergy, simulate);
    }

    public int extractEnergyInternal(int desiredEnergy, boolean simulate)
    {
        final int energyStored = getEnergyStored();

        if(desiredEnergy <= 0 || energyStored <= 0) {
            return 0;
        }

        // PORT: 1.12 capped by capacity instead of stored energy, so consumePower(cost, true) passed with 1 FE stored
        final int toRemove = Math.min(energyStored, desiredEnergy);
        if(!simulate) {
            setEnergyInternal(energyStored - toRemove, EnergyActionType.REMOVE);
        }
        return toRemove;
    }

    /**
     * Called to consume power. Config overrides this and always returns true if REQUIRES_POWER=FALSE
     *
     * @param amount to remove
     * @param simulate to check call
     * @return true if amount was consumed
     */
    public boolean consumePower(int amount, boolean simulate) {
        if(!ConfigMain.REQUIRES_POWER) {
            return true;
        }
        // PORT: all or nothing. extractEnergyInternal now caps at stored energy, a failed consume must not drain a
        // partial amount (radar fed below its per tick cost would be emptied every tick and never charge up)
        if(extractEnergyInternal(amount, true) < amount) {
            return false;
        }
        if(!simulate) {
            extractEnergyInternal(amount, false);
        }
        return true;
    }

    public ItemStack dischargeItem(ItemStack itemStack) {
        final IEnergySystem system = EnergySystem.getSystem(itemStack, null);
        if(system != null && canReceive()) {
            final int energyLeftToStore = getMaxEnergyStored() - getEnergyStored();
            if(energyLeftToStore > 0) {
                // PORT: 1.12 drained the item by the free space and lost whatever was over the receive limit
                final int accept = receiveEnergy(energyLeftToStore, true);
                if(accept > 0) {
                    final int removed = system.removeEnergy(itemStack, null, accept, false);
                    if(removed > 0) {
                        receiveEnergy(removed, false);
                    }
                }
            }
        }
        return itemStack;
    }

    @Override
    public int getEnergyStored()
    {
        return energyStorage;
    }

    @Override
    public int getMaxEnergyStored()
    {
        return storageLimit.get();
    }

    @Override
    public boolean canExtract()
    {
        return Optional.ofNullable(canExtract).map(Supplier::get).orElse(false);
    }

    @Override
    public boolean canReceive()
    {
        return Optional.ofNullable(canReceive).map(Supplier::get).orElse(true);
    }

    public void setEnergyStored(int energy)
    {
       setEnergyInternal(energy, EnergyActionType.SET);
    }

    private void setEnergyInternal(int energy, EnergyActionType type) {
        final int prev = this.energyStorage;
        // PORT: compare after clamping so a no-op set (already full/empty) does not mark the tile dirty and resend packets
        final int next = Math.min(getMaxEnergyStored(), Math.max(0, energy));
        if(next != prev) {
            this.energyStorage = next;
            Optional.ofNullable(onEnergyChanged).ifPresent(f -> f.onChange(prev, next, type));
        }
    }

    @Override
    public IntTag serializeNBT(HolderLookup.Provider provider)
    {
        return IntTag.valueOf(getEnergyStored());
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, IntTag nbt)
    {
        setEnergyStored(nbt.getAsInt());
    }
}
// PORTED
