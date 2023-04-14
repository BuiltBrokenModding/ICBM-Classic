package icbm.classic.content.blocks.launcher.network;

import icbm.classic.api.launcher.IMissileLauncher;
import icbm.classic.content.blocks.launcher.base.TileLauncherBase;

import java.util.List;

public interface ILauncherComponent {

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
        return true;
    }

    default List<IMissileLauncher> getLaunchers() {
        return getNetworkNode().getLaunchers();
    }
}
