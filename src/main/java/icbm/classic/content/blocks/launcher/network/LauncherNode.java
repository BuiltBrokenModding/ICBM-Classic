package icbm.classic.content.blocks.launcher.network;

import icbm.classic.content.blocks.launcher.base.TileLauncherBase;
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

    public LauncherNode(TileEntity self) {
        this.self = self;
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

    public List<TileLauncherBase> getLaunchers() {
        return Optional.ofNullable(getNetwork()).map(LauncherNetwork::getLaunchers).orElse(Collections.EMPTY_LIST);
    }

    @Override
    public int hashCode() {
        return self.getPos().hashCode();
    }
}
