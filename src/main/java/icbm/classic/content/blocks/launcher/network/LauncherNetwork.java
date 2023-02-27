package icbm.classic.content.blocks.launcher.network;

import icbm.classic.content.blocks.launcher.base.TileLauncherBase;
import lombok.Getter;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.items.CapabilityItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

public class LauncherNetwork implements ICapabilityProvider {

    private final HashSet<LauncherNode> components = new HashSet<LauncherNode>();

    @Getter
    private final List<TileLauncherBase> launchers = new LinkedList(); // TODO move launcher to cap interface

    private final NetworkEnergyStorage energyStorage = new NetworkEnergyStorage(this);
    private final NetworkInventory inventory = new NetworkInventory(this);

    public void invalidate(LauncherNode source) {
        final HashSet<LauncherNode> components = new HashSet(this.components);
        clearData();

        // Clear existing network state
        components.forEach(LauncherNode::onNetworkInvalidate);

        // Trigger new network state
        components.stream().filter(node -> node != source).forEach(LauncherNode::connectToTiles);
    }

    protected void clearData() {
        launchers.clear();
        components.clear();
        onNetworkUpdated();
    }

    public void mergeNetwork(LauncherNetwork otherNetwork) {
        final LauncherNetwork newNetwork = new LauncherNetwork();
        this.components.forEach(newNetwork::addToNetwork);
        otherNetwork.components.forEach(newNetwork::addToNetwork);
        clearData();
    }

    public void addToNetwork(LauncherNode node) {

        if(!components.contains(node)) {
            components.add(node);

            // Setting node with this
            node.setNetwork(this);

            // Adding if launcher
            if (node.getSelf() instanceof TileLauncherBase) {
                launchers.add((TileLauncherBase) node.getSelf());
                onNetworkUpdated();
            }
        }
    }

    public void onNetworkUpdated() {
        inventory.buildInventory();
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        return capability == CapabilityEnergy.ENERGY || capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY;
    }

    @Nullable
    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        if(capability == CapabilityEnergy.ENERGY) {
            return CapabilityEnergy.ENERGY.cast(energyStorage);
        }
        else if(capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(inventory);
        }
        return null;
    }
}
