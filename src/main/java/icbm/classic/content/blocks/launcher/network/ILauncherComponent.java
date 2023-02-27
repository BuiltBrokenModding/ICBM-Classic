package icbm.classic.content.blocks.launcher.network;

import icbm.classic.content.blocks.launcher.base.TileLauncherBase;

import java.util.List;

public interface ILauncherComponent {

    LauncherNode getNetworkNode();

    default List<TileLauncherBase> getLaunchers() {
        return getNetworkNode().getLaunchers();
    }
}
