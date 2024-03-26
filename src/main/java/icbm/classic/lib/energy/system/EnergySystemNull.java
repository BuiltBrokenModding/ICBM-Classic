package icbm.classic.lib.energy.system;

import net.minecraft.core.Direction;

/**
 * Empty version of the energy system to be used for null returns
 * <p>
 * <p>
 * Created by Dark(DarkGuardsman, Robert) on 3/13/2018.
 */
final class EnergySystemNull implements IEnergySystem {
    @Override
    public boolean canSupport(Object object, Direction side) {
        return false;
    }

    @Override
    public int setEnergy(Object object, Direction side, int energy, boolean doAction) {
        return 0;
    }

    @Override
    public boolean canSetEnergyDirectly(Object object, Direction side) {
        return false;
    }

    @Override
    public int getEnergy(Object object, Direction side) {
        return 0;
    }

    @Override
    public int getCapacity(Object object, Direction side) {
        return 0;
    }

    @Override
    public int addEnergy(Object object, Direction side, int energyToAdd, boolean doAction) {
        return 0;
    }

    @Override
    public int removeEnergy(Object object, Direction side, int energyToRemove, boolean doAction) {
        return 0;
    }
}
