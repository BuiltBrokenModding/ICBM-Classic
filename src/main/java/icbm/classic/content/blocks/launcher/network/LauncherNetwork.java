package icbm.classic.content.blocks.launcher.network;

import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.actions.cause.IActionCause;
import icbm.classic.api.launcher.ILauncherSolution;
import icbm.classic.api.launcher.IMissileLauncher;
import icbm.classic.content.blocks.launcher.FiringPackage;
import icbm.classic.lib.capability.holder.ICapabilityHolderTile;
import lombok.Getter;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.Capabilities;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * PORT: no longer an ICapabilityProvider, tiles forward {@link #getCapability(BlockCapability, Direction)}
 * from their {@link ICapabilityHolderTile#getCapability(BlockCapability, Direction)}.
 */
public class LauncherNetwork {

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

    public Stream<LauncherEntry> launch(ILauncherSolution solution, IActionCause cause, boolean simulate) {
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
            final IMissileLauncher launcher = getTileCapability(node.getSelf(), ICBMClassicAPI.MISSILE_LAUNCHER_CAPABILITY, null);
            if(launcher != null) {
                launchers.add(new LauncherEntry(launcher, node.getSelf(), null));
                onNetworkUpdated();
            }
        }
    }

    public void onNetworkUpdated() {
        inventory.buildInventory();
    }

    @Nullable
    public <T> T getCapability(@Nonnull BlockCapability<T, Direction> capability, @Nullable Direction facing) {
        if(capability == Capabilities.EnergyStorage.BLOCK) {
            return (T) energyStorage;
        }
        else if(capability == Capabilities.ItemHandler.BLOCK) {
            return (T) inventory;
        }
        return null;
    }

    /**
     * Gets a capability from a tile, uses the ICBM holder directly when possible to avoid the cap cache
     * and falls back to the level lookup for other mods' tiles.
     */
    @Nullable
    public static <T> T getTileCapability(@Nullable BlockEntity tile, BlockCapability<T, Direction> capability, @Nullable Direction side) {
        if(tile == null) {
            return null;
        }
        if(tile instanceof ICapabilityHolderTile) {
            return ((ICapabilityHolderTile) tile).getCapability(capability, side);
        }
        if(tile.getLevel() != null) {
            return tile.getLevel().getCapability(capability, tile.getBlockPos(), tile.getBlockState(), tile, side);
        }
        return null;
    }
}
// PORTED
