package icbm.classic.content.blocks.launcher.network;

import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.launcher.IActionStatus;
import icbm.classic.api.launcher.ILauncherSolution;
import icbm.classic.api.launcher.IMissileLauncher;
import icbm.classic.api.missiles.cause.IMissileCause;
import icbm.classic.api.missiles.parts.IMissileTarget;
import icbm.classic.content.blocks.launcher.FiringPackage;
import icbm.classic.content.blocks.launcher.LauncherSolution;
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
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LauncherNetwork implements ICapabilityProvider {

    @Getter
    private final HashSet<LauncherNode> components = new HashSet<LauncherNode>();

    @Getter
    private final List<LauncherEntry> launchers = new LinkedList();

    public final NetworkEnergyStorage energyStorage = new NetworkEnergyStorage(this);
    public final NetworkInventory inventory = new NetworkInventory(this);

    public void invalidate(LauncherNode source) {
        final HashSet<LauncherNode> components = new HashSet(this.components);
        clearData();

        // Clear existing network state
        components.forEach(LauncherNode::onNetworkInvalidate);

        // Trigger new network state
        components.stream().filter(node -> node != source).forEach(LauncherNode::connectToTiles);
    }

    public List<LauncherEntry> getLaunchers(int group) {
        return streamLaunchers(group).collect(Collectors.toList());
    }

    public Stream<LauncherEntry> streamLaunchers(int group) {
        return launchers.stream().filter(l -> group < 0 || l.getLauncher().getLauncherGroup() == group);
    }

    public Stream<LauncherEntry> launch(ILauncherSolution solution, IMissileCause cause, boolean simulate) {
        //TODO consider pre-checking launchers to calculate actual firing count rather than desired... for inaccuracy reasons
        return streamLaunchers(solution.getFiringGroup()).peek(entry -> {
            entry.setLastFiringPackage(new FiringPackage(solution.getTarget(entry.getLauncher()), cause));
            entry.setLastFiringTime(System.currentTimeMillis());
            entry.setLastFiringStatus(entry.getLauncher().launch(solution, cause, simulate));
            //TODO consider wrapping solution with a static version to prevent mutability problems
        });
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
            if (node.getSelf().hasCapability(ICBMClassicAPI.MISSILE_LAUNCHER_CAPABILITY, null)) {
                final IMissileLauncher launcher = node.getSelf().getCapability(ICBMClassicAPI.MISSILE_LAUNCHER_CAPABILITY, null);
                if(launcher != null) {
                    launchers.add(new LauncherEntry(launcher, node.getSelf(), null));
                    onNetworkUpdated();
                }
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
