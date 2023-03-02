package icbm.classic.content.blocks.launcher.network;

import icbm.classic.ICBMClassic;
import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.content.blocks.launcher.base.TileLauncherBase;
import lombok.AllArgsConstructor;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@AllArgsConstructor
public class NetworkEnergyStorage implements IEnergyStorage {

    private final LauncherNetwork network;

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {

        int actualReceived = 0;
        int launcherCount = 0;

        final List<IEnergyStorage> nodes = powerNodes().collect(Collectors.toList());;
        for(IEnergyStorage storage: nodes) {
            final int launchersLeft = nodes.size() - (launcherCount++);
            final int energyLeft = maxReceive - actualReceived;

            // Add 1 to prevent rounding from going to zero if launcherCount > energy
            //      Mekanism cables will do 1 FE sim checks causing this to be zero
            final int energyToGive = Math.min(energyLeft, ((int)Math.floor(energyLeft / (float) launchersLeft)) + 1);

            actualReceived += storage.receiveEnergy(energyToGive, simulate);
        };
        return actualReceived;
    }

    private Stream<IEnergyStorage> powerNodes() {
        return network.getComponents().stream()
            .map(LauncherNode::getSelf)
            .filter(tile -> tile.hasCapability(CapabilityEnergy.ENERGY, null))
            .filter(tile -> tile.hasCapability(ICBMClassicAPI.MISSILE_LAUNCHER_CAPABILITY, null))
            .map(tile -> tile.getCapability(CapabilityEnergy.ENERGY, null))
            .filter(Objects::nonNull);
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        //NOOP
        return 0;
    }

    @Override
    public int getEnergyStored() {
        return powerNodes().mapToInt(IEnergyStorage::getEnergyStored).sum();
    }

    @Override
    public int getMaxEnergyStored() {
        return powerNodes().mapToInt(IEnergyStorage::getMaxEnergyStored).sum();
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
