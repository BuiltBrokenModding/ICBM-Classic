package icbm.classic.lib.energy;

import icbm.classic.api.energy.IEnergyBuffer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.energy.IEnergyStorage;

/**
 * Basic implementation of energy buffer
 * Created by Dark on 8/15/2015.
 */
public class EnergyBuffer implements IEnergyBuffer, IEnergyStorage
{
    private final int maxStorage;
    private int energyStorage;

    public EnergyBuffer(int maxStorage)
    {
        this.maxStorage = maxStorage;
    }

    @Override
    public int addEnergyToStorage(int energy, boolean doAction)
    {
        int prev = getEnergyStored();
        if (energy > 0)
        {
            int roomLeft = getMaxBufferSize() - getEnergyStored();
            if (energy < roomLeft)
            {
                if (doAction)
                {
                    energyStorage += energy;
                    if (prev != energyStorage)
                    {
                        onPowerChange(prev, getEnergyStored(), EnergyActionType.ADD);
                    }
                }
                return energy;
            }
            else
            {
                if (doAction)
                {
                    energyStorage = getMaxBufferSize();
                    if (prev != energyStorage)
                    {
                        onPowerChange(prev, getEnergyStored(), EnergyActionType.ADD);
                    }
                }
                return roomLeft;
            }
        }
        return 0;
    }

    @Override
    public int removeEnergyFromStorage(int energy, boolean doAction)
    {
        int prev = getEnergyStored();
        if (energy > 0 && getEnergyStored() > 0)
        {
            if (energy >= getEnergyStored())
            {
                if (doAction)
                {
                    energyStorage = 0;
                    if (prev != getEnergyStored())
                    {
                        onPowerChange(prev, getEnergyStored(), EnergyActionType.REMOVE);
                    }
                }
                return getMaxBufferSize();
            }
            else
            {
                if (doAction)
                {
                    energyStorage -= energy;
                    if (prev != getEnergyStored())
                    {
                        onPowerChange(prev, getEnergyStored(), EnergyActionType.REMOVE);
                    }
                }
                return energy;
            }
        }
        return 0;
    }

    /**
     * Called when the power changes in the buffer
     *
     * @param prevEnergy - energy before action
     * @param current    - energy after action
     */
    protected void onPowerChange(int prevEnergy, int current, EnergyActionType actionType)
    {

    }

    @Override
    public int getMaxBufferSize()
    {
        return maxStorage;
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate)
    {
        return addEnergyToStorage(maxReceive, !simulate);
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate)
    {
        return removeEnergyFromStorage(maxExtract, !simulate);
    }

    @Override
    public int getEnergyStored()
    {
        return energyStorage;
    }

    @Override
    public int getMaxEnergyStored()
    {
        return getMaxBufferSize();
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
    public void setEnergyStored(int energy)
    {
        int prev = getEnergyStored();
        this.energyStorage = Math.min(getMaxBufferSize(), Math.max(0, energy));
        if (prev != energyStorage)
        {
            onPowerChange(prev, getEnergyStored(), EnergyActionType.SET);
        }
    }

    /**
     * Called to remove energy from the item and add it to this storage
     * <p>
     * Helper method to simplify the need to call {@link UniversalEnergySystem} directly
     *
     * @param stackInSlot - stack
     */
    public void addEmeryFromItem(ItemStack stackInSlot)
    {
        if (UniversalEnergySystem.isHandler(stackInSlot, null))
        {
            int energy = (int) Math.floor(UniversalEnergySystem.drain(stackInSlot, Integer.MAX_VALUE, false));
            if (energy > 0)
            {
                UniversalEnergySystem.drain(stackInSlot, addEnergyToStorage(energy, true), true);
            }
        }
    }

    public static enum EnergyActionType
    {
        ADD,
        REMOVE,
        SET
    }
}
