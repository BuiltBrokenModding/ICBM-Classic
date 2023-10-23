package icbm.classic.content.blocks.launcher.network;

public interface ILauncherComponent { //TODO move to capability system

    LauncherNode getNetworkNode();

    /**
     * Checks if the component is valid for connection
     *
     * Often want to return false when the block is removed. This will
     * prevent the network from re-adding the tile.
     *
     * @return false if invalid
     */
    default boolean isValid() {
        if(getNetworkNode() != null) {
            return !getNetworkNode().isInvalid();
        }
        return true;
    }
}
