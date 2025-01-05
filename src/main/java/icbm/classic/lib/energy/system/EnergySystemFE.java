package icbm.classic.lib.energy.system;

import icbm.classic.ICBMClassic;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.energy.IEnergyStorage;

import java.lang.reflect.Field;

/**
 *
 * Created by Dark(DarkGuardsman, Robin) on 3/13/2018.
 */
public class EnergySystemFE implements IEnergySystem
{
    private Field energyStorageField;
    private boolean failedEnergyStorageField;

    @Override
    public boolean canSupport(Object object, Direction side)
    {
        if (object instanceof TileEntity)
        {
            return ((TileEntity) object).getCapability(CapabilityEnergy.ENERGY, side).isPresent();
        }
        else if (object instanceof Entity)
        {
            return ((Entity) object).getCapability(CapabilityEnergy.ENERGY, side).isPresent();
        }
        else if (object instanceof ItemStack)
        {
            return ((ItemStack) object).getCapability(CapabilityEnergy.ENERGY, side).isPresent();
        }
        return false;
    }

    public LazyOptional<IEnergyStorage> getCapability(Object object, Direction side)
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
        return LazyOptional.empty();
    }

    @Override
    public int setEnergy(Object object, Direction side, int energy, boolean simulate)
    {
        final LazyOptional<IEnergyStorage> storageLazy = getCapability(object, side);

        if (storageLazy.isPresent())
        {
            final IEnergyStorage storage = storageLazy.orElseThrow(IllegalStateException::new);
            int energyLimited = Math.max(0, Math.min(storage.getMaxEnergyStored(), energy));

            //Edge case work around to help remove all energy, yes this will need to be done per mod
            if (storage instanceof EnergyStorage)
            {
                if (!failedEnergyStorageField)
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
                        failedEnergyStorageField = true;
                        ICBMClassic.logger().error("Failed to access EnergyStorage#energy to set energy value directly", ex);
                    }
                }
            }
            return removeEnergy(object, side, Integer.MAX_VALUE, simulate);
        }
        return 0;
    }

    @Override
    public boolean canSetEnergyDirectly(Object object, Direction side)
    {
        return !failedEnergyStorageField && getCapability(object, side).map(e -> e instanceof EnergyStorage).orElse(false);
    }

    @Override
    public int getEnergy(Object object, Direction side)
    {
        return getCapability(object, side).map(IEnergyStorage::getEnergyStored).orElse(0);
    }

    @Override
    public int getCapacity(Object object, Direction side)
    {
        return getCapability(object, side).map(IEnergyStorage::getMaxEnergyStored).orElse(0);
    }

    @Override
    public int addEnergy(Object object, Direction side, int energyToAdd, boolean simulate)
    {
        return getCapability(object, side).map((e) -> e.receiveEnergy(energyToAdd, simulate)).orElse(0);
    }

    @Override
    public int removeEnergy(Object object, Direction side, int energyToRemove, boolean simulate)
    {
        return getCapability(object, side).map((e) -> e.receiveEnergy(energyToRemove, simulate)).orElse(0);
    }
}
