package icbm.classic.lib.capability.launcher;

import icbm.classic.api.actions.cause.IActionCause;
import icbm.classic.api.actions.status.IActionStatus;
import icbm.classic.api.launcher.ILauncherSolution;
import icbm.classic.api.launcher.IMissileLauncher;
import icbm.classic.api.missiles.parts.IMissileTarget;
import icbm.classic.content.blocks.launcher.status.LauncherStatus;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * Created by Dark(DarkGuardsman, Robin) on 1/7/19.
 */
public class CapabilityMissileLauncher implements IMissileLauncher
{
    // PORT: register() (CapabilityManager storage) removed, not needed in NeoForge

    @Override
    public UUID getUniqueId() {
        return null;
    }

    @Override
    public IActionStatus getStatus() {
        return LauncherStatus.ERROR_GENERIC;
    }

    @Override
    public IActionStatus preCheckLaunch(IMissileTarget target, @Nullable IActionCause cause) {
        return LauncherStatus.ERROR_GENERIC;
    }

    @Override
    public IActionStatus launch(ILauncherSolution firingSolution, @Nullable IActionCause cause, boolean simulate) {
        return LauncherStatus.ERROR_GENERIC;
    }
}
// PORTED
