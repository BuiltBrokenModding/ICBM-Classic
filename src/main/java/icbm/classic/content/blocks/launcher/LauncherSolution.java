package icbm.classic.content.blocks.launcher;

import icbm.classic.api.launcher.ILauncherSolution;
import icbm.classic.api.launcher.IMissileLauncher;
import icbm.classic.api.missiles.parts.IMissileTarget;
import lombok.Data;

@Data
public class LauncherSolution implements ILauncherSolution {

    private final IMissileTarget target;
    private final int firingGroup;
    private final int firingCount;

    public LauncherSolution(IMissileTarget target) {
        this.target = target;
        this.firingGroup = -1;
        this.firingCount = -1;
    }

    public LauncherSolution(IMissileTarget target, int group, int firingCount) {
        this.target = target;
        this.firingGroup = group;
        this.firingCount = firingCount;
    }

    @Override
    public IMissileTarget getTarget(IMissileLauncher launcher) {
        return target;
    }
}
