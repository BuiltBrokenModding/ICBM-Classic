package icbm.classic.lib.energy.storage;

import icbm.classic.config.ConfigMain;
import icbm.classic.lib.energy.system.EnergySystem;
import icbm.classic.lib.energy.system.IEnergySystem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagInt;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.energy.IEnergyStorage;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * Basic implementation of energy buffer
 * Created by Dark on 8/15/2015.
 */
public class EnergyBuffer implements IEnergyStorage, INBTSerializable<NBTTagInt>
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

        final int maxEnergy = getMaxEnergyStored();
        final int energyStored = getEnergyStored();

        if(desiredEnergy <= 0 || energyStored <= 0) {
            return 0;
        }

        final int toRemove = Math.min(maxEnergy, desiredEnergy);
        if(!simulate) {
            setEnergyInternal(energyStored - desiredEnergy, EnergyActionType.REMOVE);
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
        return !ConfigMain.REQUIRES_POWER || extractEnergyInternal(amount, simulate) >= amount;
    }

    public ItemStack dischargeItem(ItemStack itemStack) {
        final IEnergySystem system = EnergySystem.getSystem(itemStack, null);
        if(system != null && canReceive()) {
            final int energyLeftToStore = getMaxEnergyStored() - getEnergyStored(); //TODO limit based on receive method
            if(energyLeftToStore > 0) {
                final int removed = system.removeEnergy(itemStack, null, energyLeftToStore, false);
                if(removed > 0) {
                    receiveEnergy(removed, false);
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
        if(energy != prev) {
            this.energyStorage = Math.min(getMaxEnergyStored(), Math.max(0, energy));
            Optional.ofNullable(onEnergyChanged).ifPresent(f -> f.onChange(prev, energy, type));
        }
    }

    @Override
    public NBTTagInt serializeNBT()
    {
        return new NBTTagInt(getEnergyStored());
    }

    @Override
    public void deserializeNBT(NBTTagInt nbt)
    {
        setEnergyStored(nbt.getInt());
    }
}
