package icbm.classic.lib.energy.system;

import icbm.classic.ICBMClassic;
import icbm.classic.content.reg.ComponentReg;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.EnergyStorage;
import net.neoforged.neoforge.energy.IEnergyStorage;

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
        return getCapability(object, side) != null;
    }

    public IEnergyStorage getCapability(Object object, Direction side)
    {
        if (object instanceof BlockEntity tile)
        {
            final Level level = tile.getLevel();
            if (level == null)
            {
                return null;
            }
            return level.getCapability(Capabilities.EnergyStorage.BLOCK, tile.getBlockPos(), tile.getBlockState(), tile, side);
        }
        else if (object instanceof Entity entity)
        {
            return entity.getCapability(Capabilities.EnergyStorage.ENTITY, side); // PORT-CHECK: EnergyStorage.ENTITY is sided (@Nullable Direction context) in 21.1
        }
        else if (object instanceof ItemStack stack)
        {
            if (stack.isEmpty())
            {
                return null;
            }
            return stack.getCapability(Capabilities.EnergyStorage.ITEM);
        }
        return null;
    }

    @Override
    public int setEnergy(Object object, Direction side, int energy, boolean simulate)
    {
        IEnergyStorage storage = getCapability(object, side);

        if (storage != null)
        {
            int energyLimited = Math.max(0, Math.min(storage.getMaxEnergyStored(), energy));

            //Edge case work around to help remove all energy, yes this will need to be done per mod
            if (object instanceof ItemStack stack && stack.has(ComponentReg.ENERGY.get()))
            {
                // PORT: 1.21 item energy (ComponentEnergyStorage) lives in a data component, set it directly
                if (!simulate)
                {
                    stack.set(ComponentReg.ENERGY.get(), energyLimited);
                }
            }
            else if (storage instanceof EnergyStorage)
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
        if (object instanceof ItemStack stack && stack.has(ComponentReg.ENERGY.get()))
        {
            return true;
        }
        return !failedEnergyStorageField && getCapability(object, side) instanceof EnergyStorage;
    }

    @Override
    public int getEnergy(Object object, Direction side)
    {
        IEnergyStorage storage = getCapability(object, side);
        if (storage != null)
        {
            return storage.getEnergyStored();
        }
        return 0;
    }

    @Override
    public int getCapacity(Object object, Direction side)
    {
        IEnergyStorage storage = getCapability(object, side);
        if (storage != null)
        {
            return storage.getMaxEnergyStored();
        }
        return 0;
    }

    @Override
    public int addEnergy(Object object, Direction side, int energyToAdd, boolean simulate)
    {
        IEnergyStorage storage = getCapability(object, side);
        if (storage != null)
        {
            return storage.receiveEnergy(energyToAdd, simulate);
        }
        return 0;
    }

    @Override
    public int removeEnergy(Object object, Direction side, int energyToRemove, boolean simulate)
    {
        IEnergyStorage storage = getCapability(object, side);
        if (storage != null)
        {
            return storage.extractEnergy(energyToRemove, simulate);
        }
        return 0;
    }
}
// PORTED
