package icbm.classic.lib.energy.system;

import icbm.classic.ICBMClassic;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;
import net.neoforged.energy.CapabilityEnergy;
import net.neoforged.energy.EnergyStorage;
import net.neoforged.energy.IEnergyStorage;

import java.lang.reflect.Field;

/**
 * Created by Dark(DarkGuardsman, Robert) on 3/13/2018.
 */
public class EnergySystemFE implements IEnergySystem {
    private Field energyStorageField;
    private boolean failedEnergyStorageField;

    @Override
    public boolean canSupport(Object object, Direction side) {
        if (object instanceof BlockEntity) {
            return ((BlockEntity) object).hasCapability(CapabilityEnergy.ENERGY, side);
        } else if (object instanceof Entity) {
            return ((Entity) object).hasCapability(CapabilityEnergy.ENERGY, side);
        } else if (object instanceof ItemStack) {
            return ((ItemStack) object).hasCapability(CapabilityEnergy.ENERGY, side);
        }
        return false;
    }

    public IEnergyStorage getCapability(Object object, Direction side) {
        if (object instanceof BlockEntity) {
            return ((BlockEntity) object).getCapability(CapabilityEnergy.ENERGY, side);
        } else if (object instanceof Entity) {
            return ((Entity) object).getCapability(CapabilityEnergy.ENERGY, side);
        } else if (object instanceof ItemStack) {
            return ((ItemStack) object).getCapability(CapabilityEnergy.ENERGY, side);
        }
        return null;
    }

    @Override
    public int setEnergy(Object object, Direction side, int energy, boolean simulate) {
        IEnergyStorage storage = getCapability(object, side);

        if (storage != null) {
            int energyLimited = Math.max(0, Math.min(storage.getMaxEnergyStored(), energy));

            //Edge case work around to help remove all energy, yes this will need to be done per mod
            if (storage instanceof EnergyStorage) {
                if (!failedEnergyStorageField) {
                    try {
                        if (energyStorageField == null) {
                            energyStorageField = EnergyStorage.class.getDeclaredField("energy");
                            energyStorageField.setAccessible(true);
                        }
                        energyStorageField.setInt(storage, energyLimited);
                    } catch (Exception ex) {
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
    public boolean canSetEnergyDirectly(Object object, Direction side) {
        return !failedEnergyStorageField && getCapability(object, side) instanceof EnergyStorage;
    }

    @Override
    public int getEnergy(Object object, Direction side) {
        IEnergyStorage storage = getCapability(object, side);
        if (storage != null) {
            return storage.getEnergyStored();
        }
        return 0;
    }

    @Override
    public int getCapacity(Object object, Direction side) {
        IEnergyStorage storage = getCapability(object, side);
        if (storage != null) {
            return storage.getMaxEnergyStored();
        }
        return 0;
    }

    @Override
    public int addEnergy(Object object, Direction side, int energyToAdd, boolean simulate) {
        IEnergyStorage storage = getCapability(object, side);
        if (storage != null) {
            return storage.receiveEnergy(energyToAdd, simulate);
        }
        return 0;
    }

    @Override
    public int removeEnergy(Object object, Direction side, int energyToRemove, boolean simulate) {
        IEnergyStorage storage = getCapability(object, side);
        if (storage != null) {
            return storage.extractEnergy(energyToRemove, simulate);
        }
        return 0;
    }
}
