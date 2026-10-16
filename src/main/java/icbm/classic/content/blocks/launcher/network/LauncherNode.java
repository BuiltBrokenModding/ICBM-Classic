package icbm.classic.content.blocks.launcher.network;

import lombok.Data;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

@Data
public class LauncherNode {

    private LauncherNetwork network;

    private final BlockEntity self;

    /**
     * True to allow external tiles to insert items into this tile using the network.
     * Do not allow insertion into the network and accepting of items in the same tile.
     * Other wise it will infinite loop
     * */
    private final boolean acceptsItems;

    private boolean isInvalid = false;

    /**
     * Creates a new node in the network
     *
     * @param self hosting the node
     * @param acceptsItems to enable network to feed this tile items. If enabled this tile shouldn't dump back to the network.
     */
    public LauncherNode(BlockEntity self, boolean acceptsItems) {
        this.self = self;
        this.acceptsItems = acceptsItems;
    }

    public void setNetwork(LauncherNetwork network) {
        if(this.network != network) {
            this.network = network;
            invalidateCapabilities();
        }
    }

    /**
     * PORT: 1.21 caches block capabilities, network backed caps (energy/items) change with the network
     */
    protected void invalidateCapabilities() {
        final Level level = self.getLevel();
        if(level != null && !level.isClientSide()) {
            level.invalidateCapabilities(self.getBlockPos());
        }
    }

    /**
     * Invalidate reference to self and blocks around
     */
    public void onTileRemoved() {
        final LauncherNetwork currentNetwork = network;
        if (currentNetwork != null) {
            network = null;
            isInvalid = true;
            currentNetwork.invalidate(this);
        }
    }

    public void onNetworkInvalidate() {
        setNetwork(null);
    }

    public void connectToTiles() {
        final Level level = self.getLevel();
        if(level == null) {
            return;
        }
        for (Direction side : Direction.values()) {
            final BlockPos nextPos = self.getBlockPos().relative(side);

            // Only search for tiles in loaded chunks
            if (level.isLoaded(nextPos)) {

                //Get next possible connection
                final BlockEntity tile = level.getBlockEntity(nextPos);
                if (tile instanceof ILauncherComponent && ((ILauncherComponent) tile).isValid()) {
                    final LauncherNode node = ((ILauncherComponent) tile).getNetworkNode();

                    // Ignore dead nodes
                    if(node.isInvalid()) {
                        continue;
                    }

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

    @Override
    public boolean equals(Object o) {
        return this == o;
    }

    @Override
    public int hashCode() {
        return self.getBlockPos().hashCode();
    }
}
// PORTED
