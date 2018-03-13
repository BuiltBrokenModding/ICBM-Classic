package icbm.classic.lib.energy.system;

/**
 * Empty version of the energy system to be used for null returns
 *
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 3/13/2018.
 */
final class EnergySystemNull implements IEnergySystem
{
    @Override
    public int setEnergy(Object object, int energy, boolean doAction)
    {
        return 0;
    }

    @Override
    public int getEnergy(Object object)
    {
        return 0;
    }

    @Override
    public int getCapacity(Object object)
    {
        return 0;
    }

    @Override
    public int addEnergy(Object object, int energyToAdd, boolean doAction)
    {
        return 0;
    }

    @Override
    public int removeEnergy(Object object, int energyToRemove, boolean doAction)
    {
        return 0;
    }
}
