package icbm.classic.prefab.tile;

import icbm.classic.lib.energy.storage.EnergyActionType;
import icbm.classic.lib.energy.storage.EnergyBuffer;

public class PowerBuffer<M extends TilePoweredMachine> extends EnergyBuffer {
    public final M machine;

    public PowerBuffer(M machine) {
        super(machine.getEnergyBufferSize());
        this.machine = machine;
    }

    @Override
    protected void onPowerChange(int prevEnergy, int current, EnergyActionType actionType) {
        super.onPowerChange(prevEnergy, current, actionType);
        machine.updateClient = true;
    }

    @Override
    public int getMaxBufferSize() {
        return machine.getEnergyBufferSize();
    }
}
