package icbm.classic.lib.energy.system;

import net.minecraft.util.EnumFacing;

/**
 * Empty version of the energy system to be used for null returns
 *
 *
 * Created by Dark(DarkGuardsman, Robert) on 3/13/2018.
 */
final class EnergySystemNull implements IEnergySystem
{
    @Override
    public boolean canSupport(Object object, EnumFacing side)
    {
        return false;
    }

    @Override
    public int setEnergy(Object object, EnumFacing side, int energy, boolean doAction)
    {
        return 0;
    }

    @Override
    public boolean canSetEnergyDirectly(Object object, EnumFacing side)
    {
        return false;
    }

    @Override
    public int getEnergy(Object object, EnumFacing side)
    {
        return 0;
    }

    @Override
    public int getCapacity(Object object, EnumFacing side)
    {
        return 0;
    }

    @Override
    public int addEnergy(Object object, EnumFacing side, int energyToAdd, boolean doAction)
    {
        return 0;
    }

    @Override
    public int removeEnergy(Object object, EnumFacing side, int energyToRemove, boolean doAction)
    {
        return 0;
    }
}
