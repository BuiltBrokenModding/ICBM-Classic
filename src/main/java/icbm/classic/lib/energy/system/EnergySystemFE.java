package icbm.classic.lib.energy.system;

import icbm.classic.ICBMClassic;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.energy.IEnergyStorage;

import java.lang.reflect.Field;

/**
 *
 * Created by Dark(DarkGuardsman, Robert) on 3/13/2018.
 */
public class EnergySystemFE implements IEnergySystem
{
    private Field energyStorageField;
    private boolean failedenergyStorageField;

    @Override
    public boolean canSupport(Object object, EnumFacing side)
    {
        if (object instanceof TileEntity)
        {
            return ((TileEntity) object).hasCapability(CapabilityEnergy.ENERGY, side);
        }
        else if (object instanceof Entity)
        {
            return ((Entity) object).hasCapability(CapabilityEnergy.ENERGY, side);
        }
        else if (object instanceof ItemStack)
        {
            return ((ItemStack) object).hasCapability(CapabilityEnergy.ENERGY, side);
        }
        return false;
    }

    public IEnergyStorage getCapability(Object object, EnumFacing side)
    {
        if (object instanceof TileEntity)
        {
            return ((TileEntity) object).getCapability(CapabilityEnergy.ENERGY, side);
        }
        else if (object instanceof Entity)
        {
            return ((Entity) object).getCapability(CapabilityEnergy.ENERGY, side);
        }
        else if (object instanceof ItemStack)
        {
            return ((ItemStack) object).getCapability(CapabilityEnergy.ENERGY, side);
        }
        return null;
    }

    @Override
    public int setEnergy(Object object, EnumFacing side, int energy, boolean doAction)
    {
        IEnergyStorage storage = getCapability(object, side);

        if (storage != null)
        {
            int energyLimited = Math.max(0, Math.min(storage.getMaxEnergyStored(), energy));

            //Edge case work around to help remove all energy, yes this will need to be done per mod
            if (storage instanceof EnergyStorage)
            {
                if (!failedenergyStorageField)
                {
                    try
                    {
                        if (energyStorageField == null)
                        {
                            energyStorageField = EnergyStorage.class.getDeclaredField("energy");
                            energyStorageField.setAccessible(true);
                        }
                        energyStorageField.setInt(storage, energyLimited);
                    }
                    catch (Exception ex)
                    {
                        failedenergyStorageField = true;
                        ICBMClassic.logger().error("Failed to access EnergyStorage#energy to set energy value directly", ex);
                    }
                }
            }
            return removeEnergy(object, side, Integer.MAX_VALUE, doAction);
        }
        return 0;
    }

    @Override
    public boolean canSetEnergyDirectly(Object object, EnumFacing side)
    {
        return !failedenergyStorageField && getCapability(object, side) instanceof EnergyStorage;
    }

    @Override
    public int getEnergy(Object object, EnumFacing side)
    {
        IEnergyStorage storage = getCapability(object, side);
        if (storage != null)
        {
            return storage.getEnergyStored();
        }
        return 0;
    }

    @Override
    public int getCapacity(Object object, EnumFacing side)
    {
        IEnergyStorage storage = getCapability(object, side);
        if (storage != null)
        {
            return storage.getMaxEnergyStored();
        }
        return 0;
    }

    @Override
    public int addEnergy(Object object, EnumFacing side, int energyToAdd, boolean doAction)
    {
        IEnergyStorage storage = getCapability(object, side);
        if (storage != null)
        {
            return storage.receiveEnergy(energyToAdd, !doAction);
        }
        return 0;
    }

    @Override
    public int removeEnergy(Object object, EnumFacing side, int energyToRemove, boolean doAction)
    {
        IEnergyStorage storage = getCapability(object, side);
        if (storage != null)
        {
            return storage.extractEnergy(energyToRemove, doAction);
        }
        return 0;
    }
}
