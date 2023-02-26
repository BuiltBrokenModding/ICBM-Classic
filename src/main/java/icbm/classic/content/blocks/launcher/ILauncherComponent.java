package icbm.classic.content.blocks.launcher;

import icbm.classic.content.blocks.launcher.base.TileLauncherBase;

public interface ILauncherComponent {

    LauncherReference getReference();

    default TileLauncherBase getLauncher() {
        return getReference().findHost(null);
    }
}
