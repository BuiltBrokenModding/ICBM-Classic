package icbm.classic.world.block.launcher.network;

public interface ILauncherComponent { //TODO move to capability system

    LauncherNode getNetworkNode();

    /**
     * Checks if the component is valid for connection
     * <p>
     * Often want to return false when the block is removed. This will
     * prevent the network from re-adding the tile.
     *
     * @return false if invalid
     */
    default boolean isValid() {
        if (getNetworkNode() != null) {
            return !getNetworkNode().isInvalid();
        }
        return true;
    }
}
