package icbm.classic.content.blocks.launcher.network;

import icbm.classic.api.launcher.IMissileLauncher;
import icbm.classic.content.blocks.launcher.base.TileLauncherBase;

import java.util.List;

public interface ILauncherComponent {

    LauncherNode getNetworkNode();

    default List<IMissileLauncher> getLaunchers() {
        return getNetworkNode().getLaunchers();
    }
}
