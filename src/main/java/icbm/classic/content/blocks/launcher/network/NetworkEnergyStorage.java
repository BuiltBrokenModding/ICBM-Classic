package icbm.classic.content.blocks.launcher.network;

import icbm.classic.content.blocks.launcher.base.TileLauncherBase;
import lombok.AllArgsConstructor;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

import java.util.List;

@AllArgsConstructor
public class NetworkEnergyStorage implements IEnergyStorage {

    private final LauncherNetwork network;

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        final List<TileLauncherBase> launchers = network.getLaunchers();

        int actualReceived = 0;
        int launcherCount = 0;

        for(TileLauncherBase launcher : launchers) {
            final int launchersLeft = launchers.size() - (launcherCount++);
            final int energyLeft = maxReceive - actualReceived;

            final int energyToGive = Math.min(energyLeft, ((int)Math.floor(energyLeft / (float) launchersLeft)) + 1); // Add +1 to prevent zero

            if(launcher.hasCapability(CapabilityEnergy.ENERGY, null)) {
                final IEnergyStorage storage = launcher.getCapability(CapabilityEnergy.ENERGY, null);
                if(storage != null) {
                    actualReceived += storage.receiveEnergy(energyToGive, simulate);
                }
            }
        }
        return actualReceived;
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        //NOOP
        return 0;
    }

    @Override
    public int getEnergyStored() {
        return network.getLaunchers().stream().mapToInt(TileLauncherBase::getEnergy).sum();
    }

    @Override
    public int getMaxEnergyStored() {
        return network.getLaunchers().stream().mapToInt(TileLauncherBase::getEnergyBufferSize).sum();
    }

    @Override
    public boolean canExtract() {
        return false;
    }

    @Override
    public boolean canReceive() {
        return true;
    }
}
