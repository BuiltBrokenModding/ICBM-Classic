package icbm.classic.content.blocks.launcher.network;

import icbm.classic.api.launcher.IMissileLauncher;
import lombok.Data;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Data
public class LauncherNode {

    private LauncherNetwork network;

    private final TileEntity self;

    /**
     * True to allow external tiles to insert items into this tile using the network.
     * Do not allow insertion into the network and accepting of items in the same tile.
     * Other wise it will infinite loop
     * */
    private final boolean acceptsItems;

    /**
     * Creates a new node in the network
     *
     * @param self hosting the node
     * @param acceptsItems to enable network to feed this tile items. If enabled this tile shouldn't dump back to the network.
     */
    public LauncherNode(TileEntity self, boolean acceptsItems) {
        this.self = self;
        this.acceptsItems = acceptsItems;
    }

    /**
     * Invalidate reference to self and blocks around
     */
    public void onTileRemoved() {
        if (network != null) {
            network.invalidate(this);
        }
    }

    public void onNetworkInvalidate() {
        network = null;
    }

    public void connectToTiles() {
        for (EnumFacing side : EnumFacing.VALUES) {
            final BlockPos nextPos = self.getPos().offset(side);

            // Only search for tiles in loaded chunks
            if (self.getWorld().isBlockLoaded(nextPos)) {

                //Get next possible connection
                final TileEntity tile = self.getWorld().getTileEntity(nextPos);
                if (tile instanceof ILauncherComponent) {
                    final LauncherNode node = ((ILauncherComponent) tile).getNetworkNode();

                    // Case: no network, found network
                    if (this.getNetwork() == null && node.getNetwork() != null) {
                        node.getNetwork().addToNetwork(this);
                    }
                    // Case: has network, node without network
                    else if (this.getNetwork() != null && node.getNetwork() == null) {
                        this.getNetwork().addToNetwork(node);
                    }
                    // Case: no network, node without network
                    else if (this.getNetwork() == null && node.getNetwork() == null) {
                        final LauncherNetwork newNetwork = new LauncherNetwork();
                        newNetwork.addToNetwork(this);
                        newNetwork.addToNetwork(node);
                    }
                    // Case: has network, node has different network
                    else if (this.getNetwork() != null && node.getNetwork() != this.getNetwork()) {
                        this.getNetwork().mergeNetwork(node.getNetwork());
                    }
                    // Case: both match, ignore
                }
            }
        }
    }

    public List<IMissileLauncher> getLaunchers() {
        return Optional.ofNullable(getNetwork()).map(LauncherNetwork::getLaunchers).orElse(Collections.EMPTY_LIST);
    }

    @Override
    public int hashCode() {
        return self.getPos().hashCode();
    }
}
